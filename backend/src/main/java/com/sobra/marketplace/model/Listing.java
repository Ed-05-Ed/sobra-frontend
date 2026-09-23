package com.sobra.marketplace.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.sobra.inventory.model.Food;
import com.sobra.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 300)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "location_name", length = 160)
    private String locationName;

    protected Listing() {
    }

    public Listing(
            Food food,
            User owner,
            ListingType type,
            BigDecimal price,
            String description,
            Double latitude,
            Double longitude,
            String locationName,
            Instant createdAt
    ) {
        this.id = UUID.randomUUID();
        this.food = Objects.requireNonNull(food);
        this.owner = Objects.requireNonNull(owner);
        this.type = Objects.requireNonNull(type);
        this.status = ListingStatus.ACTIVE;
        this.price = price;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public Food getFood() {
        return food;
    }

    public User getOwner() {
        return owner;
    }

    public ListingType getType() {
        return type;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void reserve() {
        this.status = ListingStatus.RESERVED;
    }

    public void activate() {
        this.status = ListingStatus.ACTIVE;
    }

    public void close() {
        this.status = ListingStatus.CLOSED;
    }
    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocationName() {
        return locationName;
    }
}