package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.insight.InsightResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.AIInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
@Tag(name = "AI Insights", description = "Rule-based financial insights and recommendations")
public class InsightController {

    private final AIInsightService aiInsightService;
    private final AuthenticatedUserProvider userProvider;

    @GetMapping
    @Operation(summary = "Generate current financial insights for the user")
    public ResponseEntity<ApiResponse<List<InsightResponse>>> insights(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(aiInsightService.generateInsights(user)));
    }
}
