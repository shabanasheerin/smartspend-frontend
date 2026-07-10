package com.smartspend.service;

import com.smartspend.dto.expense.ExpenseRequest;
import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.entity.Category;
import com.smartspend.entity.Expense;
import com.smartspend.entity.User;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.mapper.ExpenseMapper;
import com.smartspend.repository.CategoryRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final BudgetService budgetService;

    @Transactional
    public ExpenseResponse create(User user, ExpenseRequest request) {
        Category category = resolveCategory(user, request.getCategoryId());

        if (request.isRecurring() && request.getRecurrenceFrequency() == null) {
            throw new BadRequestException("Recurrence frequency is required for recurring expenses");
        }

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .notes(request.getNotes())
                .recurring(request.isRecurring())
                .recurrenceFrequency(request.getRecurrenceFrequency())
                .nextRecurrenceDate(request.isRecurring()
                        ? nextDate(request.getExpenseDate(), request.getRecurrenceFrequency())
                        : null)
                .build();

        Expense saved = expenseRepository.save(expense);
        budgetService.checkAndNotify(user, category.getId(), expense.getExpenseDate());
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(User user, Long expenseId, ExpenseRequest request) {
        Expense expense = findOwned(user, expenseId);
        Category category = resolveCategory(user, request.getCategoryId());

        if (request.isRecurring() && request.getRecurrenceFrequency() == null) {
            throw new BadRequestException("Recurrence frequency is required for recurring expenses");
        }

        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNotes(request.getNotes());
        expense.setRecurring(request.isRecurring());
        expense.setRecurrenceFrequency(request.getRecurrenceFrequency());
        expense.setNextRecurrenceDate(request.isRecurring()
                ? nextDate(request.getExpenseDate(), request.getRecurrenceFrequency())
                : null);

        Expense saved = expenseRepository.save(expense);
        budgetService.checkAndNotify(user, category.getId(), expense.getExpenseDate());
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public void delete(User user, Long expenseId) {
        Expense expense = findOwned(user, expenseId);
        expenseRepository.delete(expense);
    }

    public ExpenseResponse getById(User user, Long expenseId) {
        return expenseMapper.toResponse(findOwned(user, expenseId));
    }

    public Page<ExpenseResponse> search(User user,
                                         Long categoryId,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         BigDecimal minAmount,
                                         BigDecimal maxAmount,
                                         String search,
                                         Pageable pageable) {
        return expenseRepository
                .findAll(ExpenseSpecification.filter(user, categoryId, startDate, endDate, minAmount, maxAmount, search), pageable)
                .map(expenseMapper::toResponse);
    }

    public BigDecimal totalForPeriod(User user, LocalDate start, LocalDate end) {
        return expenseRepository.sumByUserAndDateRange(user, start, end);
    }

    public BigDecimal totalForCategoryAndPeriod(User user, Long categoryId, LocalDate start, LocalDate end) {
        return expenseRepository.sumByUserAndCategoryAndDateRange(user, categoryId, start, end);
    }

    private Category resolveCategory(User user, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean ownedByUser = category.getUser() != null && category.getUser().getId().equals(user.getId());
        if (!category.isSystemDefined() && !ownedByUser) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private Expense findOwned(User user, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Expense record not found");
        }
        return expense;
    }

    private LocalDate nextDate(LocalDate from, Expense.RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
        };
    }
}
