package com.sobra.dashboard.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sobra.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/community")
    public CommunityDashboardResponse getCommunityDashboard() {
        return dashboardService.getCommunityDashboard();
    }

    @GetMapping
    public DashboardResponse getDashboard(
            @RequestParam UUID userId
    ) {
        return dashboardService.getUserDashboard(userId);
    }
}