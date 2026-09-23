package com.sobra.inventory.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
        name = "food_movements",
        uniqueConstraints = @UniqueConstraint(name = "uk_food_movements_operation_id", columnNames = "operation_id")
)
public class FoodMovement {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "operation_id", nullable = false, updatable = false)
    private UUID operationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false, updatable = false)
    private Food food;

    @NotNull
    @Positive
    @Digits(integer = 9, fraction = 3)
    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private Unit unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private MovementType type;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "was_priority_at_consumption", nullable = false, updatable = false)
    private boolean wasPriorityAtConsumption;

    @Column(name = "expected_version", updatable = false)
    private Long expectedVersion;

    @Column(name = "remaining_quantity_after", updatable = false, precision = 12, scale = 3)
    private BigDecimal remainingQuantityAfter;

    @Column(name = "food_version_after", updatable = false)
    private Long foodVersionAfter;

    protected FoodMovement() {
    }

    public FoodMovement(
            UUID operationId,
            Food food,
            BigDecimal quantity,
            Unit unit,
            MovementType type,
            Instant occurredAt,
            boolean wasPriorityAtConsumption,
            long expectedVersion,
            BigDecimal remainingQuantityAfter,
            long foodVersionAfter
    ) {
        this.id = UUID.randomUUID();
        this.operationId = Objects.requireNonNull(operationId);
        this.food = Objects.requireNonNull(food);
        this.quantity = Objects.requireNonNull(quantity);
        this.unit = Objects.requireNonNull(unit);
        this.type = Objects.requireNonNull(type);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.wasPriorityAtConsumption = wasPriorityAtConsumption;
        this.expectedVersion = expectedVersion;
        this.remainingQuantityAfter = Objects.requireNonNull(remainingQuantityAfter);
        this.foodVersionAfter = foodVersionAfter;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public Food getFood() {
        return food;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public MovementType getType() {
        return type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public boolean wasPriorityAtConsumption() {
        return wasPriorityAtConsumption;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public BigDecimal getRemainingQuantityAfter() {
        return remainingQuantityAfter;
    }

    public Long getFoodVersionAfter() {
        return foodVersionAfter;
    }
}
