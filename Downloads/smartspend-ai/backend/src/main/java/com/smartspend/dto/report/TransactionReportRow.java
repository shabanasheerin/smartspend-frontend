package com.smartspend.dto.report;

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
public class TransactionReportRow {
    private LocalDate date;
    private String type;      // INCOME or EXPENSE
    private String category;  // category name or income source
    private BigDecimal amount;
    private String notes;
}
