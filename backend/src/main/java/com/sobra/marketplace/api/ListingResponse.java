package com.sobra.marketplace.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.sobra.marketplace.model.ListingStatus;
import com.sobra.marketplace.model.ListingType;

public record ListingResponse(
        UUID id,
        UUID foodId,
        UUID ownerId,
        String ownerName,
        String foodName,
        String ingredientName,
        ListingType type,
        ListingStatus status,
        BigDecimal price,
        String description,
        LocalDate labelDate,
        Double latitude,
        Double longitude,
        String locationName,
        Instant createdAt
) {
}