package com.sobra.inventory.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.sobra.inventory.model.DateType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFoodRequest(
        UUID userId,
        @NotNull UUID ingredientId,
        @NotBlank String name,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 9, fraction = 3) BigDecimal quantity,
        @NotNull LocalDate labelDate,
        @NotNull DateType dateType
) {
}