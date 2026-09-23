package com.sobra.reservation.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.sobra.marketplace.model.Listing;
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
@Table(name = "reservations")
public class Reservation {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "pickup_code", nullable = false, length = 4)
    private String pickupCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Reservation() {
    }

    public Reservation(
            Listing listing,
            User user,
            String pickupCode,
            Instant createdAt
    ) {
        this.id = UUID.randomUUID();
        this.listing = Objects.requireNonNull(listing);
        this.user = Objects.requireNonNull(user);
        this.status = ReservationStatus.ACTIVE;
        this.pickupCode = Objects.requireNonNull(pickupCode);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public User getUser() {
        return user;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
}
