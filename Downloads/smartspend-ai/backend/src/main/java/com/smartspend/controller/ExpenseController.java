package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.common.PagedResponse;
import com.smartspend.dto.expense.ExpenseRequest;
import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense", description = "Manage expense records")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping
    @Operation(summary = "Add a new expense record")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @Valid @RequestBody ExpenseRequest request) {
        User user = userProvider.resolve(principal);
        ExpenseResponse response = expenseService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Expense added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense record")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody ExpenseRequest request) {
        User user = userProvider.resolve(principal);
        ExpenseResponse response = expenseService.update(user, id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense record")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        expenseService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense record by id")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(expenseService.getById(user, id)));
    }

    @GetMapping
    @Operation(summary = "Search/filter/paginate expense records")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> search(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "expenseDate") Pageable pageable) {

        User user = userProvider.resolve(principal);
        Page<ExpenseResponse> page = expenseService.search(user, categoryId, startDate, endDate, minAmount, maxAmount, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}
