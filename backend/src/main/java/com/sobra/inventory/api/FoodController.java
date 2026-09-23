package com.sobra.inventory.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sobra.inventory.api.dto.CreateFoodRequest;
import com.sobra.inventory.api.dto.CreateMovementRequest;
import com.sobra.inventory.api.dto.FoodResponse;
import com.sobra.inventory.api.dto.MovementResponse;
import com.sobra.inventory.api.dto.UpdateFoodRequest;
import com.sobra.inventory.service.InventoryService;
import com.sobra.inventory.service.MovementService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import com.sobra.inventory.api.dto.ExpiringFoodResponse;

@Validated
@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final InventoryService inventoryService;
    private final MovementService movementService;

    public FoodController(InventoryService inventoryService, MovementService movementService) {
        this.inventoryService = inventoryService;
        this.movementService = movementService;
    }

    @GetMapping
    public List<FoodResponse> listFoods(
            @RequestParam(required = false) UUID userId
    ) {
        return inventoryService.listFoods(userId);
    }

    @GetMapping("/{id}")
    public FoodResponse getFood(@PathVariable UUID id) {
        return inventoryService.getFood(id);
    }

    @PostMapping
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody CreateFoodRequest request) {
        FoodResponse response = inventoryService.createFood(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public FoodResponse updateFood(@PathVariable UUID id, @Valid @RequestBody UpdateFoodRequest request) {
        return inventoryService.updateFood(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveFood(
            @PathVariable UUID id,
            @RequestParam @PositiveOrZero long version
    ) {
        inventoryService.archiveFood(id, version);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/movements")
    public MovementResponse registerMovement(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMovementRequest request
    ) {
        return movementService.register(id, request);
    }
    @GetMapping("/expiring")
    public List<ExpiringFoodResponse> listExpiring(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "5") int days
    ) {
        return inventoryService.listExpiringFoods(userId, days);
    }
}
