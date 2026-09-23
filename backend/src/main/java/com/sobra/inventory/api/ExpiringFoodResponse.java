package com.sobra.inventory.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.sobra.inventory.model.ExpirationUrgency;

public record ExpiringFoodResponse(
        UUID foodId,
        String name,
        LocalDate labelDate,
        long daysRemaining,
        ExpirationUrgency urgency,
        String recommendation
) {
}