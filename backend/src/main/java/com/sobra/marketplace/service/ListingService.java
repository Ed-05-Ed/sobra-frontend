package com.sobra.marketplace.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.inventory.model.Food;
import com.sobra.inventory.repository.FoodRepository;
import com.sobra.marketplace.api.CreateListingRequest;
import com.sobra.marketplace.api.ListingResponse;
import com.sobra.marketplace.model.Listing;
import com.sobra.marketplace.model.ListingStatus;
import com.sobra.marketplace.model.ListingType;
import com.sobra.marketplace.repository.ListingRepository;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final FoodRepository foodRepository;
    private final Clock clock;

    public ListingService(
            ListingRepository listingRepository,
            FoodRepository foodRepository,
            Clock clock
    ) {
        this.listingRepository = listingRepository;
        this.foodRepository = foodRepository;
        this.clock = clock;
    }

    @Transactional
    public ListingResponse create(CreateListingRequest request) {
        validatePrice(request.type(), request.price());
        validateLocation(request.latitude(), request.longitude());

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.FOOD_NOT_FOUND,
                        "El alimento solicitado no existe."
                ));

        if (food.isArchived()) {
            throw ApiException.conflict(
                    ErrorCode.FOOD_ARCHIVED,
                    "No se puede publicar un alimento archivado."
            );
        }

        Listing listing = new Listing(
                food,
                food.getOwner(),
                request.type(),
                request.price(),
                normalizeDescription(request.description()),
                request.latitude(),
                request.longitude(),
                normalizeLocationName(request.locationName()),
                Instant.now(clock)
        );

        return toResponse(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> listActive() {
        return listingRepository
                .findAllByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> listByOwner(UUID userId) {
        return listingRepository
                .findAllByOwnerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ListingResponse close(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.LISTING_NOT_FOUND,
                        "La publicación solicitada no existe."
                ));

        listing.close();

        return toResponse(listing);
    }

    private void validatePrice(ListingType type, BigDecimal price) {
        if (type == ListingType.SALE) {
            if (price == null || price.signum() <= 0) {
                throw ApiException.badRequest(
                        "Las publicaciones de venta requieren un precio mayor a cero."
                );
            }
        }

        if (type == ListingType.DONATION && price != null) {
            throw ApiException.badRequest(
                    "Las donaciones no deben incluir precio."
            );
        }
    }

    private void validateLocation(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }

        if (latitude == null || longitude == null) {
            throw ApiException.badRequest(
                    "La ubicación debe incluir latitud y longitud."
            );
        }

        if (latitude < -90 || latitude > 90) {
            throw ApiException.badRequest(
                    "La latitud debe estar entre -90 y 90."
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw ApiException.badRequest(
                    "La longitud debe estar entre -180 y 180."
            );
        }
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeLocationName(String locationName) {
        if (locationName == null) {
            return null;
        }

        String normalized = locationName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private ListingResponse toResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getFood().getId(),
                listing.getOwner().getId(),
                listing.getOwner().getName(),
                listing.getFood().getName(),
                listing.getFood().getIngredient().getName(),
                listing.getType(),
                listing.getStatus(),
                listing.getPrice(),
                listing.getDescription(),
                listing.getFood().getLabelDate(),
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getLocationName(),
                listing.getCreatedAt()
        );
    }
}