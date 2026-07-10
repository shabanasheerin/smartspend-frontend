package com.smartspend.repository;

import com.smartspend.entity.Income;
import com.smartspend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    List<Income> findByUserAndIncomeDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndDateRange(User user, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i")
    BigDecimal sumAllAmounts();
}
