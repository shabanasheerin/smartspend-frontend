package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.receipt.ReceiptResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses/{expenseId}/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Attach and manage receipt files for an expense")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Upload a receipt file (JPEG/PNG/WEBP/PDF, max 5MB) for an expense")
    public ResponseEntity<ApiResponse<ReceiptResponse>> upload(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long expenseId,
                                                                @RequestParam("file") MultipartFile file) {
        User user = userProvider.resolve(principal);
        ReceiptResponse response = receiptService.attachToExpense(user, expenseId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Receipt uploaded", response));
    }

    @GetMapping
    @Operation(summary = "List receipts attached to an expense")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal,
                                                                    @PathVariable Long expenseId) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(receiptService.listForExpense(user, expenseId)));
    }

    @DeleteMapping("/{receiptId}")
    @Operation(summary = "Delete a receipt from an expense")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long expenseId,
                                                     @PathVariable Long receiptId) {
        User user = userProvider.resolve(principal);
        receiptService.delete(user, expenseId, receiptId);
        return ResponseEntity.ok(ApiResponse.success("Receipt deleted", null));
    }
}
