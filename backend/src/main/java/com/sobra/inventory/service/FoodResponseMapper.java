package com.sobra.inventory.service;

import org.springframework.stereotype.Component;

import com.sobra.inventory.api.dto.FoodResponse;
import com.sobra.inventory.model.Food;
import com.sobra.inventory.service.InventoryDatePolicy.DateInformation;

@Component
public class FoodResponseMapper {

    private final InventoryDatePolicy datePolicy;

    public FoodResponseMapper(InventoryDatePolicy datePolicy) {
        this.datePolicy = datePolicy;
    }

    public FoodResponse toResponse(Food food) {
        DateInformation dateInformation = datePolicy.evaluate(food.getLabelDate());
        return new FoodResponse(
                food.getId(),
                food.getIngredient().getId(),
                food.getName(),
                food.getRemainingQuantity(),
                food.getIngredient().getUnit(),
                food.getLabelDate(),
                food.getDateType(),
                food.getCreatedAt(),
                food.getUpdatedAt(),
                food.getVersion(),
                dateInformation.status(),
                dateInformation.daysUntilLabelDate()
        );
    }
}
