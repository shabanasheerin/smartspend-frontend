package com.smartspend.service;

import com.smartspend.dto.insight.InsightResponse;
import com.smartspend.entity.Expense;
import com.smartspend.entity.User;
import com.smartspend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rule-based financial insights engine.
 *
 * Deliberately kept as a clean, single-responsibility service with no
 * dependency on any particular analysis technique — each insight is generated
 * by an isolated private method. This makes it straightforward to later swap
 * or augment individual rules with calls to an LLM API (e.g. summarizing the
 * same underlying numbers in natural language, or ranking insights by
 * relevance) without touching the rest of the module.
 */
@Service
@RequiredArgsConstructor
public class AIInsightService {

    private final ExpenseRepository expenseRepository;

    public List<InsightResponse> generateInsights(User user) {
        List<InsightResponse> insights = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<Expense> currentMonthExpenses = expenseRepository.findByUserAndExpenseDateBetween(
                user, currentMonth.atDay(1), currentMonth.atEndOfMonth());
        List<Expense> previousMonthExpenses = expenseRepository.findByUserAndExpenseDateBetween(
                user, previousMonth.atDay(1), previousMonth.atEndOfMonth());

        addHighestCategoryInsight(insights, currentMonthExpenses);
        addMonthOverMonthInsight(insights, currentMonthExpenses, previousMonthExpenses);
        addEndOfMonthPredictionInsight(insights, currentMonthExpenses, currentMonth);
        addDailyAverageInsight(insights, currentMonthExpenses, currentMonth);

        return insights;
    }

    private void addHighestCategoryInsight(List<InsightResponse> insights, List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return;
        }

        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        Map.Entry<String, BigDecimal> top = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();

        insights.add(InsightResponse.builder()
                .type("HIGHEST_CATEGORY")
                .title("Highest Spending Category")
                .message(String.format("Your highest spending category this month is %s, totaling %.2f",
                        top.getKey(), top.getValue()))
                .build());
    }

    private void addMonthOverMonthInsight(List<InsightResponse> insights,
                                           List<Expense> currentMonthExpenses,
                                           List<Expense> previousMonthExpenses) {
        BigDecimal currentTotal = sum(currentMonthExpenses);
        BigDecimal previousTotal = sum(previousMonthExpenses);

        if (previousTotal.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal diffPercent = currentTotal.subtract(previousTotal)
                .multiply(BigDecimal.valueOf(100))
                .divide(previousTotal, 2, RoundingMode.HALF_UP);

        String direction = diffPercent.compareTo(BigDecimal.ZERO) >= 0 ? "higher" : "lower";

        insights.add(InsightResponse.builder()
                .type("MONTH_OVER_MONTH")
                .title("Compared to Last Month")
                .message(String.format("You've spent %.1f%% %s than last month so far.",
                        diffPercent.abs(), direction))
                .build());
    }

    private void addEndOfMonthPredictionInsight(List<InsightResponse> insights,
                                                 List<Expense> currentMonthExpenses,
                                                 YearMonth currentMonth) {
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        int daysInMonth = currentMonth.lengthOfMonth();

        if (dayOfMonth == 0 || currentMonthExpenses.isEmpty()) {
            return;
        }

        BigDecimal currentTotal = sum(currentMonthExpenses);
        BigDecimal dailyAverage = currentTotal.divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP);
        BigDecimal predicted = dailyAverage.multiply(BigDecimal.valueOf(daysInMonth));

        insights.add(InsightResponse.builder()
                .type("PREDICTED_END_OF_MONTH")
                .title("Predicted End-of-Month Spending")
                .message(String.format("At your current pace, you're projected to spend around %.2f by the end of the month.",
                        predicted))
                .build());
    }

    private void addDailyAverageInsight(List<InsightResponse> insights,
                                         List<Expense> currentMonthExpenses,
                                         YearMonth currentMonth) {
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        if (currentMonthExpenses.isEmpty() || dayOfMonth == 0) {
            return;
        }

        BigDecimal currentTotal = sum(currentMonthExpenses);
        BigDecimal dailyAverage = currentTotal.divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP);

        insights.add(InsightResponse.builder()
                .type("DAILY_AVERAGE")
                .title("Daily Average Spending")
                .message(String.format("You're averaging %.2f per day in spending this month.", dailyAverage))
                .build());
    }

    private BigDecimal sum(List<Expense> expenses) {
        return expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
