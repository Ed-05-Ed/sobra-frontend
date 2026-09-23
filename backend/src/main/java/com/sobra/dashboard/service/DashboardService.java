package com.sobra.dashboard.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.dashboard.api.DashboardResponse;
import com.sobra.inventory.repository.FoodRepository;
import com.sobra.marketplace.model.ListingStatus;
import com.sobra.marketplace.model.ListingType;
import com.sobra.marketplace.repository.ListingRepository;
import com.sobra.reservation.model.ReservationStatus;
import com.sobra.reservation.repository.ReservationRepository;
import com.sobra.dashboard.api.CommunityDashboardResponse;

@Service
public class DashboardService {

    private final FoodRepository foodRepository;
    private final ListingRepository listingRepository;
    private final ReservationRepository reservationRepository;

    public DashboardService(
            FoodRepository foodRepository,
            ListingRepository listingRepository,
            ReservationRepository reservationRepository
    ) {
        this.foodRepository = foodRepository;
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
    }
    @Transactional(readOnly = true)
    public CommunityDashboardResponse getCommunityDashboard() {

        long activeListings =
                listingRepository.countByStatus(
                        ListingStatus.ACTIVE
                );

        long completedExchanges =
                reservationRepository.countByStatus(
                        ReservationStatus.COMPLETED
                );

        long completedDonations =
                reservationRepository.countByStatusAndListingType(
                        ReservationStatus.COMPLETED,
                        ListingType.DONATION
                );

        long completedSales =
                reservationRepository.countByStatusAndListingType(
                        ReservationStatus.COMPLETED,
                        ListingType.SALE
                );

        return new CommunityDashboardResponse(
                activeListings,
                completedExchanges,
                completedDonations,
                completedSales
        );
    }

    @Transactional(readOnly = true)
    public DashboardResponse getUserDashboard(UUID userId) {

        long foodsInInventory =
                foodRepository.countByOwnerIdAndArchivedFalse(userId);

        long activeListings =
                listingRepository.countByOwnerIdAndStatus(
                        userId,
                        ListingStatus.ACTIVE
                );

        long activeReservations =
                reservationRepository.countByUserIdAndStatus(
                        userId,
                        ReservationStatus.ACTIVE
                );

        long completedExchanges =
                reservationRepository.countByListingOwnerIdAndStatus(
                        userId,
                        ReservationStatus.COMPLETED
                );

        long completedDonations =
                reservationRepository
                        .countByListingOwnerIdAndStatusAndListingType(
                                userId,
                                ReservationStatus.COMPLETED,
                                ListingType.DONATION
                        );

        long completedSales =
                reservationRepository
                        .countByListingOwnerIdAndStatusAndListingType(
                                userId,
                                ReservationStatus.COMPLETED,
                                ListingType.SALE
                        );

        return new DashboardResponse(
                foodsInInventory,
                activeListings,
                activeReservations,
                completedExchanges,
                completedDonations,
                completedSales
        );
    }
}