package com.sobra.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sobra.inventory.model.Food;

public interface FoodRepository extends JpaRepository<Food, UUID> {

    @EntityGraph(attributePaths = "ingredient")
    List<Food> findAllByArchivedFalseOrderByLabelDateAscIdAsc();

    @EntityGraph(attributePaths = "ingredient")
    Optional<Food> findByIdAndArchivedFalse(UUID id);

    @Override
    @EntityGraph(attributePaths = "ingredient")
    Optional<Food> findById(UUID id);

    long countByOwnerIdAndArchivedFalse(UUID ownerId);

    List<Food> findAllByOwnerIdAndArchivedFalseAndLabelDateBetweenOrderByLabelDateAsc(
            UUID ownerId,
            LocalDate startDate,
            LocalDate endDate
    );

    @EntityGraph(attributePaths = "ingredient")
    List<Food> findAllByOwnerIdAndArchivedFalseOrderByLabelDateAscIdAsc(UUID ownerId);
}
