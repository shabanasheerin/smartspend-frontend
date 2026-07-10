package com.smartspend.dto.income;

import com.smartspend.entity.Income;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeResponse {
    private Long id;
    private BigDecimal amount;
    private Income.IncomeSource source;
    private LocalDate incomeDate;
    private String notes;
    private LocalDateTime createdAt;
}
