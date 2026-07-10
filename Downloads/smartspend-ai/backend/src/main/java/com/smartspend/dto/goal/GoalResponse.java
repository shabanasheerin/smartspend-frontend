package com.smartspend.dto.goal;

import com.smartspend.entity.SavingsGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal progressPercentage;
    private LocalDate targetDate;
    private LocalDate estimatedCompletionDate;
    private SavingsGoal.GoalStatus status;
}
