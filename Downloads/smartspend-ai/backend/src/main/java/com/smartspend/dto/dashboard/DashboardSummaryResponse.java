package com.smartspend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal currentBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalSavings;
    private Map<String, BigDecimal> categoryDistribution;
    private List<MonthlyTrendPoint> monthlyExpenseTrend;
    private List<MonthlyTrendPoint> monthlyIncomeTrend;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendPoint {
        private String month; // e.g. "2026-07"
        private BigDecimal amount;
    }
}
