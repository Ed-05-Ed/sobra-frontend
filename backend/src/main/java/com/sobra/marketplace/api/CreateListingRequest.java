package com.sobra.marketplace.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.sobra.marketplace.model.ListingType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateListingRequest(
        @NotNull UUID foodId,
        @NotNull ListingType type,
        BigDecimal price,
        @Size(max = 300) String description,

        Double latitude,
        Double longitude,

        @Size(max = 160)
        String locationName
) {
}