package com.sobra.reservation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sobra.marketplace.model.ListingType;
import com.sobra.reservation.model.ReservationStatus;

import com.sobra.reservation.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @EntityGraph(attributePaths = {
            "listing",
            "listing.food",
            "listing.food.ingredient",
            "listing.owner",
            "user"
    })
    List<Reservation> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndStatus(UUID userId, ReservationStatus status);

    long countByListingOwnerIdAndStatus(UUID ownerId, ReservationStatus status);
    long countByListingOwnerIdAndStatusAndListingType(
            UUID ownerId,
            ReservationStatus status,
            ListingType type
    );

    long countByStatus(ReservationStatus status);

    long countByStatusAndListingType(
            ReservationStatus status,
            ListingType type
    );


    @EntityGraph(attributePaths = {
            "listing",
            "listing.food",
            "listing.food.ingredient",
            "listing.owner",
            "user"
    })
    List<Reservation> findAllByListingOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}