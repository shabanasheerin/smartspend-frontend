package com.smartspend.dto.income;

import com.smartspend.entity.Income;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomeRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Income source is required")
    private Income.IncomeSource source;

    @NotNull(message = "Income date is required")
    @PastOrPresent(message = "Income date cannot be in the future")
    private LocalDate incomeDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
