package com.smartspend.service;

import com.smartspend.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.entity.Expense;
import com.smartspend.entity.SavingsGoal;
import com.smartspend.entity.User;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.IncomeRepository;
import com.smartspend.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int TREND_MONTHS = 6;

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public DashboardSummaryResponse getSummary(User user) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        BigDecimal monthlyIncome = incomeRepository.sumByUserAndDateRange(user, monthStart, monthEnd);
        BigDecimal monthlyExpenses = expenseRepository.sumByUserAndDateRange(user, monthStart, monthEnd);

        BigDecimal allTimeIncome = incomeRepository.sumByUserAndDateRange(user, LocalDate.of(2000, 1, 1), LocalDate.now());
        BigDecimal allTimeExpenses = expenseRepository.sumByUserAndDateRange(user, LocalDate.of(2000, 1, 1), LocalDate.now());
        BigDecimal currentBalance = allTimeIncome.subtract(allTimeExpenses);

        BigDecimal totalSavings = savingsGoalRepository.findByUser(user).stream()
                .map(SavingsGoal::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryDistribution = expenseRepository
                .findByUserAndExpenseDateBetween(user, monthStart, monthEnd)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        List<DashboardSummaryResponse.MonthlyTrendPoint> expenseTrend = new ArrayList<>();
        List<DashboardSummaryResponse.MonthlyTrendPoint> incomeTrend = new ArrayList<>();

        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            BigDecimal expTotal = expenseRepository.sumByUserAndDateRange(user, ym.atDay(1), ym.atEndOfMonth());
            BigDecimal incTotal = incomeRepository.sumByUserAndDateRange(user, ym.atDay(1), ym.atEndOfMonth());

            expenseTrend.add(DashboardSummaryResponse.MonthlyTrendPoint.builder()
                    .month(ym.format(MONTH_FORMAT)).amount(expTotal).build());
            incomeTrend.add(DashboardSummaryResponse.MonthlyTrendPoint.builder()
                    .month(ym.format(MONTH_FORMAT)).amount(incTotal).build());
        }

        return DashboardSummaryResponse.builder()
                .currentBalance(currentBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .totalSavings(totalSavings)
                .categoryDistribution(categoryDistribution)
                .monthlyExpenseTrend(expenseTrend)
                .monthlyIncomeTrend(incomeTrend)
                .build();
    }
}
