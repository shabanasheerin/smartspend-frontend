package com.smartspend.service;

import com.smartspend.dto.budget.BudgetRequest;
import com.smartspend.entity.Budget;
import com.smartspend.entity.Category;
import com.smartspend.entity.User;
import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.repository.BudgetRepository;
import com.smartspend.repository.CategoryRepository;
import com.smartspend.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private BudgetService budgetService;

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        return u;
    }

    private Category buildCategory() {
        Category c = Category.builder().name("Food").type(Category.CategoryType.EXPENSE).systemDefined(true).build();
        c.setId(1L);
        return c;
    }

    @Test
    void create_throwsDuplicateResourceException_whenBudgetAlreadyExistsForMonth() {
        User user = buildUser();
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("5000"), 7, 2026, null);

        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, 1L, 7, 2026))
                .thenReturn(Optional.of(new Budget()));

        assertThrows(DuplicateResourceException.class, () -> budgetService.create(user, request));
    }

    @Test
    void create_savesNewBudget_whenNoDuplicateExists() {
        User user = buildUser();
        Category category = buildCategory();
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("5000"), 7, 2026, new BigDecimal("80"));

        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, 1L, 7, 2026))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.sumByUserAndCategoryAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        var response = budgetService.create(user, request);

        assertEquals("Food", response.getCategoryName());
        assertEquals(new BigDecimal("5000"), response.getLimitAmount());
    }

    @Test
    void percentageUsed_isZero_whenNoExpensesYet() {
        User user = buildUser();
        Category category = buildCategory();
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("1000"), 7, 2026, new BigDecimal("80"));

        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, 1L, 7, 2026))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.sumByUserAndCategoryAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = budgetService.create(user, request);

        assertEquals(0, BigDecimal.ZERO.compareTo(response.getPercentageUsed()));
        assertFalse(response.isAlertTriggered());
    }
}
