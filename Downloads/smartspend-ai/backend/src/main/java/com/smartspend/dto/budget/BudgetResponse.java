package com.smartspend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private BigDecimal percentageUsed;
    private Integer month;
    private Integer year;
    private BigDecimal alertThresholdPercent;
    private boolean alertTriggered;
}
