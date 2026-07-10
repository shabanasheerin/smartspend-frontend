package com.smartspend.service;

import com.smartspend.dto.receipt.ReceiptResponse;
import com.smartspend.entity.Expense;
import com.smartspend.entity.Receipt;
import com.smartspend.entity.User;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ExpenseRepository expenseRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ReceiptResponse attachToExpense(User user, Long expenseId, MultipartFile file) {
        Expense expense = findOwnedExpense(user, expenseId);

        FileStorageService.StoredFile stored = fileStorageService.storeReceiptFile(file);

        Receipt receipt = Receipt.builder()
                .expense(expense)
                .fileName(stored.originalFileName())
                .fileUrl(stored.fileUrl())
                .contentType(stored.contentType())
                .fileSizeBytes(stored.fileSizeBytes())
                .build();

        return toResponse(receiptRepository.save(receipt));
    }

    public List<ReceiptResponse> listForExpense(User user, Long expenseId) {
        findOwnedExpense(user, expenseId);
        return receiptRepository.findByExpenseId(expenseId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(User user, Long expenseId, Long receiptId) {
        findOwnedExpense(user, expenseId);

        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));

        if (!receipt.getExpense().getId().equals(expenseId)) {
            throw new ResourceNotFoundException("Receipt not found");
        }

        fileStorageService.deleteFile(receipt.getFileUrl());
        receiptRepository.delete(receipt);
    }

    private Expense findOwnedExpense(User user, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Expense not found");
        }
        return expense;
    }

    private ReceiptResponse toResponse(Receipt receipt) {
        return ReceiptResponse.builder()
                .id(receipt.getId())
                .expenseId(receipt.getExpense().getId())
                .fileName(receipt.getFileName())
                .fileUrl(receipt.getFileUrl())
                .contentType(receipt.getContentType())
                .fileSizeBytes(receipt.getFileSizeBytes())
                .createdAt(receipt.getCreatedAt())
                .build();
    }
}
