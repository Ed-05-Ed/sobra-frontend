package com.sobra.inventory.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sobra.inventory.model.MovementType;
import com.sobra.inventory.model.Unit;

public record MovementResponse(
        UUID movementId,
        UUID operationId,
        UUID foodId,
        MovementType type,
        BigDecimal quantity,
        Unit unit,
        Instant occurredAt,
        boolean wasPriorityAtConsumption,
        BigDecimal remainingQuantityAfter,
        long foodVersionAfter
) {
}
