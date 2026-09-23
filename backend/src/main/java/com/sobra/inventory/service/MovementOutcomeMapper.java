package com.sobra.inventory.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sobra.inventory.api.dto.CreateMovementRequest;
import com.sobra.inventory.api.dto.MovementResponse;
import com.sobra.inventory.model.FoodMovement;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;

@Component
public class MovementOutcomeMapper {

    public MovementResponse replayOrConflict(
            FoodMovement movement,
            UUID requestedFoodId,
            CreateMovementRequest request
    ) {
        boolean sameRequest = movement.getExpectedVersion() != null
                && movement.getRemainingQuantityAfter() != null
                && movement.getFoodVersionAfter() != null
                && movement.getFood().getId().equals(requestedFoodId)
                && movement.getType() == request.type()
                && movement.getQuantity().compareTo(request.quantity()) == 0
                && movement.getExpectedVersion().longValue() == request.expectedVersion().longValue();
        if (!sameRequest) {
            throw ApiException.conflict(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "El operationId ya fue utilizado con otro contenido.");
        }
        return toResponse(movement);
    }

    public MovementResponse toResponse(FoodMovement movement) {
        if (movement.getRemainingQuantityAfter() == null || movement.getFoodVersionAfter() == null) {
            throw ApiException.conflict(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "El operationId pertenece a un movimiento histórico sin resultado reproducible.");
        }
        return new MovementResponse(
                movement.getId(),
                movement.getOperationId(),
                movement.getFood().getId(),
                movement.getType(),
                movement.getQuantity(),
                movement.getUnit(),
                movement.getOccurredAt(),
                movement.wasPriorityAtConsumption(),
                movement.getRemainingQuantityAfter(),
                movement.getFoodVersionAfter()
        );
    }
}
