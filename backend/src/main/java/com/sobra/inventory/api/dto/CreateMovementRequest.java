package com.sobra.inventory.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sobra.inventory.model.MovementType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateMovementRequest(
        @NotNull UUID operationId,
        @NotNull MovementType type,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3) BigDecimal quantity,
        @NotNull @PositiveOrZero Long expectedVersion
) {
}
