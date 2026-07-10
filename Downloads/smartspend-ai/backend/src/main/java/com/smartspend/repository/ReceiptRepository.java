package com.smartspend.repository;

import com.smartspend.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByExpenseId(Long expenseId);
}
