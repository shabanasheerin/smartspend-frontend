package com.smartspend.service;

import com.smartspend.dto.budget.BudgetRequest;
import com.smartspend.dto.budget.BudgetResponse;
import com.smartspend.entity.Budget;
import com.smartspend.entity.Category;
import com.smartspend.entity.Notification;
import com.smartspend.entity.User;
import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.BudgetRepository;
import com.smartspend.repository.CategoryRepository;
import com.smartspend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationService notificationService;

    @Transactional
    public BudgetResponse create(User user, BudgetRequest request) {
        budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, request.getCategoryId(), request.getMonth(), request.getYear())
                .ifPresent(b -> {
                    throw new DuplicateResourceException("A budget already exists for this category and month");
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .limitAmount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .alertThresholdPercent(request.getAlertThresholdPercent() != null
                        ? request.getAlertThresholdPercent() : new BigDecimal("80.00"))
                .build();

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(User user, Long budgetId, BudgetRequest request) {
        Budget budget = findOwned(user, budgetId);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        budget.setCategory(category);
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        if (request.getAlertThresholdPercent() != null) {
            budget.setAlertThresholdPercent(request.getAlertThresholdPercent());
        }

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(User user, Long budgetId) {
        budgetRepository.delete(findOwned(user, budgetId));
    }

    public List<BudgetResponse> listForMonth(User user, Integer month, Integer year) {
        return budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Called after an expense is created/updated to check budget thresholds
     * and fire a BUDGET_EXCEEDED notification when the alert threshold is crossed.
     */
    @Transactional
    public void checkAndNotify(User user, Long categoryId, LocalDate expenseDate) {
        budgetRepository.findByUserAndCategoryIdAndMonthAndYear(
                user, categoryId, expenseDate.getMonthValue(), expenseDate.getYear())
                .ifPresent(budget -> {
                    BigDecimal used = usedAmount(user, budget);
                    BigDecimal percent = percentageUsed(used, budget.getLimitAmount());
                    if (percent.compareTo(budget.getAlertThresholdPercent()) >= 0) {
                        notificationService.create(user,
                                "Budget Alert: " + budget.getCategory().getName(),
                                String.format("You've used %.1f%% of your %s budget for %d/%d",
                                        percent, budget.getCategory().getName(), budget.getMonth(), budget.getYear()),
                                Notification.NotificationType.BUDGET_EXCEEDED);
                    }
                });
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal used = usedAmount(budget.getUser(), budget);
        BigDecimal remaining = budget.getLimitAmount().subtract(used);
        BigDecimal percent = percentageUsed(used, budget.getLimitAmount());

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .limitAmount(budget.getLimitAmount())
                .usedAmount(used)
                .remainingAmount(remaining)
                .percentageUsed(percent)
                .month(budget.getMonth())
                .year(budget.getYear())
                .alertThresholdPercent(budget.getAlertThresholdPercent())
                .alertTriggered(percent.compareTo(budget.getAlertThresholdPercent()) >= 0)
                .build();
    }

    private BigDecimal usedAmount(User user, Budget budget) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        return expenseRepository.sumByUserAndCategoryAndDateRange(
                user, budget.getCategory().getId(), ym.atDay(1), ym.atEndOfMonth());
    }

    private BigDecimal percentageUsed(BigDecimal used, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return used.multiply(BigDecimal.valueOf(100)).divide(limit, 2, RoundingMode.HALF_UP);
    }

    private Budget findOwned(User user, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found");
        }
        return budget;
    }
}
