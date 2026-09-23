package com.sobra.inventory.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.sobra.inventory.api.dto.CreateMovementRequest;
import com.sobra.inventory.api.dto.MovementResponse;
import com.sobra.inventory.repository.FoodMovementRepository;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;

import jakarta.persistence.OptimisticLockException;

@Service
public class MovementService {

    private final FoodMovementRepository movementRepository;
    private final MovementTransactionService transactionService;
    private final MovementOutcomeMapper outcomeMapper;

    public MovementService(
            FoodMovementRepository movementRepository,
            MovementTransactionService transactionService,
            MovementOutcomeMapper outcomeMapper
    ) {
        this.movementRepository = movementRepository;
        this.transactionService = transactionService;
        this.outcomeMapper = outcomeMapper;
    }

    public MovementResponse register(UUID foodId, CreateMovementRequest request) {
        return movementRepository.findByOperationId(request.operationId())
                .map(movement -> outcomeMapper.replayOrConflict(movement, foodId, request))
                .orElseGet(() -> executeAndRecover(foodId, request));
    }

    private MovementResponse executeAndRecover(UUID foodId, CreateMovementRequest request) {
        try {
            return transactionService.execute(foodId, request);
        } catch (RuntimeException exception) {
            return movementRepository.findByOperationId(request.operationId())
                    .map(movement -> outcomeMapper.replayOrConflict(movement, foodId, request))
                    .orElseThrow(() -> translateConcurrency(exception));
        }
    }

    private RuntimeException translateConcurrency(RuntimeException exception) {
        if (hasCause(exception, OptimisticLockingFailureException.class)
                || hasCause(exception, OptimisticLockException.class)) {
            return ApiException.conflict(ErrorCode.STALE_VERSION,
                    "El alimento fue modificado por otra operación.");
        }
        if (exception instanceof DataIntegrityViolationException) {
            return ApiException.conflict(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "No fue posible confirmar la operación idempotente.");
        }
        return exception;
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
