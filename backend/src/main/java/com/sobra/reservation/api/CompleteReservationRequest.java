package com.sobra.reservation.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompleteReservationRequest(

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "El código debe tener 4 dígitos.")
        String pickupCode
) {
}