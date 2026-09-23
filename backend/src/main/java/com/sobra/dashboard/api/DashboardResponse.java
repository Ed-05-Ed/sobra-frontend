package com.sobra.dashboard.api;

public record DashboardResponse(
        long foodsInInventory,
        long activeListings,
        long activeReservations,
        long completedExchanges,
        long completedDonations,
        long completedSales
) {
}