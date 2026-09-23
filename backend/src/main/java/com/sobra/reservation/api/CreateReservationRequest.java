package com.sobra.reservation.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull UUID listingId,
        @NotNull UUID userId
) {
}