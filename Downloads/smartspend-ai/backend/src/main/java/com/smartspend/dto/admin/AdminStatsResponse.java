package com.smartspend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long totalIncomeRecords;
    private long totalExpenseRecords;
    private BigDecimal totalExpenseAmountAllUsers;
    private BigDecimal totalIncomeAmountAllUsers;
}
