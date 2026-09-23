package com.sobra.inventory.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "ingredients",
        uniqueConstraints = @UniqueConstraint(name = "uk_ingredients_name", columnNames = "name")
)
public class Ingredient {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Unit unit;

    protected Ingredient() {
    }

    public Ingredient(String name, Unit unit) {
        this.id = UUID.randomUUID();
        this.name = Objects.requireNonNull(name);
        this.unit = Objects.requireNonNull(unit);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }
}
