package com.sobra.dashboard.api;

public record CommunityDashboardResponse(
        long activeListings,
        long completedExchanges,
        long completedDonations,
        long completedSales
) {
}