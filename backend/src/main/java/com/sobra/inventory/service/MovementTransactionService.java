package com.sobra.inventory.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.inventory.api.dto.CreateMovementRequest;
import com.sobra.inventory.api.dto.MovementResponse;
import com.sobra.inventory.model.DateStatus;
import com.sobra.inventory.model.Food;
import com.sobra.inventory.model.FoodMovement;
import com.sobra.inventory.model.MovementType;
import com.sobra.inventory.repository.FoodMovementRepository;
import com.sobra.inventory.repository.FoodRepository;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;

@Service
public class MovementTransactionService {

    private final FoodRepository foodRepository;
    private final FoodMovementRepository movementRepository;
    private final OperationIdLock operationIdLock;
    private final InventoryInputValidator inputValidator;
    private final InventoryDatePolicy datePolicy;
    private final MovementOutcomeMapper outcomeMapper;
    private final Clock clock;

    public MovementTransactionService(
            FoodRepository foodRepository,
            FoodMovementRepository movementRepository,
            OperationIdLock operationIdLock,
            InventoryInputValidator inputValidator,
            InventoryDatePolicy datePolicy,
            MovementOutcomeMapper outcomeMapper,
            Clock clock
    ) {
        this.foodRepository = foodRepository;
        this.movementRepository = movementRepository;
        this.operationIdLock = operationIdLock;
        this.inputValidator = inputValidator;
        this.datePolicy = datePolicy;
        this.outcomeMapper = outcomeMapper;
        this.clock = clock;
    }

    @Transactional
    public MovementResponse execute(UUID foodId, CreateMovementRequest request) {
        operationIdLock.acquire(request.operationId());
        return movementRepository.findByOperationId(request.operationId())
                .map(movement -> outcomeMapper.replayOrConflict(movement, foodId, request))
                .orElseGet(() -> executeNew(foodId, request));
    }

    private MovementResponse executeNew(UUID foodId, CreateMovementRequest request) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.FOOD_NOT_FOUND,
                        "El alimento solicitado no existe."));
        if (food.isArchived()) {
            throw ApiException.conflict(ErrorCode.FOOD_ARCHIVED,
                    "No se admiten movimientos sobre un alimento archivado.");
        }
        if (food.getVersion() != request.expectedVersion()) {
            throw ApiException.conflict(ErrorCode.STALE_VERSION,
                    "La versión enviada ya no corresponde al alimento.");
        }

        inputValidator.validateQuantity(request.quantity(), food.getIngredient().getUnit(), "quantity");
        if (food.getRemainingQuantity().compareTo(request.quantity()) < 0) {
            throw ApiException.conflict(ErrorCode.INSUFFICIENT_QUANTITY,
                    "La cantidad solicitada supera el saldo disponible.");
        }

        Instant occurredAt = clock.instant();
        boolean wasPriorityAtConsumption = request.type() == MovementType.CONSUMED
                && datePolicy.evaluate(food.getLabelDate()).status() == DateStatus.PRIORITY;
        food.decreaseQuantity(request.quantity(), occurredAt);
        foodRepository.flush();

        BigDecimal remainingQuantityAfter = food.getRemainingQuantity();
        long foodVersionAfter = food.getVersion();
        FoodMovement movement = new FoodMovement(
                request.operationId(),
                food,
                request.quantity(),
                food.getIngredient().getUnit(),
                request.type(),
                occurredAt,
                wasPriorityAtConsumption,
                request.expectedVersion(),
                remainingQuantityAfter,
                foodVersionAfter
        );
        movementRepository.saveAndFlush(movement);
        return outcomeMapper.toResponse(movement);
    }
}
