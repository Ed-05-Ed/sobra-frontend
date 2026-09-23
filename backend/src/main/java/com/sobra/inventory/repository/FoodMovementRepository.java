package com.sobra.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sobra.inventory.model.FoodMovement;

public interface FoodMovementRepository extends JpaRepository<FoodMovement, UUID> {

    @EntityGraph(attributePaths = "food")
    Optional<FoodMovement> findByOperationId(UUID operationId);
}
