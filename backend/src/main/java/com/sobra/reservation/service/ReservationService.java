package com.sobra.reservation.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.marketplace.model.Listing;
import com.sobra.marketplace.model.ListingStatus;
import com.sobra.marketplace.repository.ListingRepository;
import com.sobra.reservation.api.CreateReservationRequest;
import com.sobra.reservation.api.ReservationResponse;
import com.sobra.reservation.model.Reservation;
import com.sobra.reservation.repository.ReservationRepository;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;
import com.sobra.user.model.User;
import com.sobra.user.repository.UserRepository;
import com.sobra.reservation.model.ReservationStatus;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponse> listReceivedByOwner(UUID ownerId) {
        return reservationRepository
                .findAllByListingOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.LISTING_NOT_FOUND,
                        "La publicación solicitada no existe."
                ));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw ApiException.conflict(
                    ErrorCode.LISTING_NOT_AVAILABLE,
                    "La publicación ya no está disponible."
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.USER_NOT_FOUND,
                        "El usuario solicitado no existe."
                ));

        if (listing.getOwner().getId().equals(user.getId())) {
            throw ApiException.badRequest(
                    "No puedes reservar tu propia publicación."
            );
        }

        String pickupCode = String.format(
                "%04d",
                ThreadLocalRandom.current().nextInt(10000)
        );

        Reservation reservation = new Reservation(
                listing,
                user,
                pickupCode,
                Instant.now(clock)
        );

        listing.reserve();

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listByUser(UUID userId) {
        return reservationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Transactional
    public ReservationResponse complete(UUID id, String pickupCode) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "La reserva solicitada no existe."
                ));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw ApiException.conflict(
                    ErrorCode.RESERVATION_NOT_ACTIVE,
                    "La reserva ya no está activa."
            );
        }

        if (!reservation.getPickupCode().equals(pickupCode)) {
            throw ApiException.badRequest(
                    "El código de recogida es incorrecto."
            );
        }

        reservation.complete();
        reservation.getListing().close();

        return toResponse(reservation);
    }
    @Transactional
    public ReservationResponse cancel(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "La reserva solicitada no existe."
                ));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw ApiException.conflict(
                    ErrorCode.RESERVATION_NOT_ACTIVE,
                    "La reserva ya no está activa."
            );
        }

        reservation.cancel();
        reservation.getListing().activate();

        return toResponse(reservation);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getListing().getId(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getListing().getOwner().getName(),
                reservation.getListing().getFood().getName(),
                reservation.getStatus(),
                reservation.getPickupCode(),
                reservation.getCreatedAt()
        );
    }
}
