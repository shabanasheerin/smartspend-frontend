package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated dashboard cards and chart data")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticatedUserProvider userProvider;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary: balances, monthly totals, category distribution, trends")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(user)));
    }
}
