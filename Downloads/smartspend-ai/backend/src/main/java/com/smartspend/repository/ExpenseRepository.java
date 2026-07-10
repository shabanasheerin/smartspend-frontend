package com.smartspend.repository;

import com.smartspend.entity.Expense;
import com.smartspend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndDateRange(User user, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category.id = :categoryId AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndCategoryAndDateRange(User user, Long categoryId, LocalDate start, LocalDate end);

    List<Expense> findByRecurringTrueAndNextRecurrenceDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllAmounts();
}
