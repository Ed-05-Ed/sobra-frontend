package com.sobra.reservation.api;

import java.time.Instant;
import java.util.UUID;

import com.sobra.reservation.model.ReservationStatus;

public record ReservationResponse(
        UUID id,
        UUID listingId,
        UUID userId,
        String userName,
        String ownerName,
        String foodName,
        ReservationStatus status,
        String pickupCode,
        Instant createdAt
) {
}