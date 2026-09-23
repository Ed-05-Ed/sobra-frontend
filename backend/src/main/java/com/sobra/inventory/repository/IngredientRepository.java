package com.sobra.inventory.repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sobra.inventory.model.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    List<Ingredient> findAllByOrderByNameAsc();

    Optional<Ingredient> findByNameIgnoreCase(String name);
}
