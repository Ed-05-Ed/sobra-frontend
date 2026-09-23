package com.sobra.inventory.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.sobra.user.model.User;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 9, fraction = 3)
    @Column(name = "remaining_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal remainingQuantity;

    @NotNull
    @Column(name = "label_date", nullable = false)
    private LocalDate labelDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "date_type", nullable = false, length = 20)
    private DateType dateType;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    private boolean archived;

    protected Food() {
    }

    public Food(
            User owner,
            Ingredient ingredient,
            String name,
            BigDecimal remainingQuantity,
            LocalDate labelDate,
            DateType dateType,
            Instant createdAt
    ) {
        this.id = UUID.randomUUID();
        this.owner = Objects.requireNonNull(owner);
        this.ingredient = Objects.requireNonNull(ingredient);
        this.name = Objects.requireNonNull(name);
        this.remainingQuantity = Objects.requireNonNull(remainingQuantity);
        this.labelDate = Objects.requireNonNull(labelDate);
        this.dateType = Objects.requireNonNull(dateType);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = createdAt;
        this.archived = false;
    }

    public UUID getId() {
        return id;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public LocalDate getLabelDate() {
        return labelDate;
    }

    public DateType getDateType() {
        return dateType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public boolean isArchived() {
        return archived;
    }

    public void updateMetadata(String name, LocalDate labelDate, DateType dateType, Instant updatedAt) {
        this.name = Objects.requireNonNull(name);
        this.labelDate = Objects.requireNonNull(labelDate);
        this.dateType = Objects.requireNonNull(dateType);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void decreaseQuantity(BigDecimal quantity, Instant updatedAt) {
        this.remainingQuantity = this.remainingQuantity.subtract(Objects.requireNonNull(quantity));
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void archive(Instant updatedAt) {
        this.archived = true;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
