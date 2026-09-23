package com.sobra.inventory.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sobra.inventory.api.dto.IngredientResponse;
import com.sobra.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final InventoryService inventoryService;

    public IngredientController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<IngredientResponse> listIngredients() {
        return inventoryService.listIngredients();
    }
}
