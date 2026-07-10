package com.smartspend.dto.expense;

import com.smartspend.entity.Expense;
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
public class ExpenseResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String notes;
    private boolean recurring;
    private Expense.RecurrenceFrequency recurrenceFrequency;
    private LocalDate nextRecurrenceDate;
    private LocalDateTime createdAt;
}
