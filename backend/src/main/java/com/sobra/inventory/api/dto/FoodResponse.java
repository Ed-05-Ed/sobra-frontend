package com.sobra.inventory.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.sobra.inventory.model.DateStatus;
import com.sobra.inventory.model.DateType;
import com.sobra.inventory.model.Unit;

public record FoodResponse(
        UUID id,
        UUID ingredientId,
        String name,
        BigDecimal remainingQuantity,
        Unit unit,
        LocalDate labelDate,
        DateType dateType,
        Instant createdAt,
        Instant updatedAt,
        long version,
        DateStatus dateStatus,
        long daysUntilLabelDate
) {
}
