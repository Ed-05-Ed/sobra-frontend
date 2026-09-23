package com.sobra.inventory.api.dto;

import java.util.UUID;

import com.sobra.inventory.model.Unit;

public record IngredientResponse(UUID id, String name, Unit unit) {
}
