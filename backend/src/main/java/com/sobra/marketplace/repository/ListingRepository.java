package com.sobra.marketplace.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sobra.marketplace.model.Listing;
import com.sobra.marketplace.model.ListingStatus;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @EntityGraph(attributePaths = {"food", "food.ingredient", "owner"})
    List<Listing> findAllByStatusOrderByCreatedAtDesc(ListingStatus status);

    @EntityGraph(attributePaths = {"food", "food.ingredient", "owner"})
    List<Listing> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    
    long countByStatus(ListingStatus status);

    long countByOwnerIdAndStatus(UUID ownerId, ListingStatus status);
}