package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.common.PagedResponse;
import com.smartspend.dto.income.IncomeRequest;
import com.smartspend.dto.income.IncomeResponse;
import com.smartspend.entity.Income;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.IncomeService;
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
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
@Tag(name = "Income", description = "Manage income records")
public class IncomeController {

    private final IncomeService incomeService;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping
    @Operation(summary = "Add a new income record")
    public ResponseEntity<ApiResponse<IncomeResponse>> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @Valid @RequestBody IncomeRequest request) {
        User user = userProvider.resolve(principal);
        IncomeResponse response = incomeService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Income added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing income record")
    public ResponseEntity<ApiResponse<IncomeResponse>> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody IncomeRequest request) {
        User user = userProvider.resolve(principal);
        IncomeResponse response = incomeService.update(user, id, request);
        return ResponseEntity.ok(ApiResponse.success("Income updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an income record")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        incomeService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.success("Income deleted", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single income record by id")
    public ResponseEntity<ApiResponse<IncomeResponse>> getById(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(incomeService.getById(user, id)));
    }

    @GetMapping
    @Operation(summary = "Search/filter/paginate income records")
    public ResponseEntity<ApiResponse<PagedResponse<IncomeResponse>>> search(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Income.IncomeSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "incomeDate") Pageable pageable) {

        User user = userProvider.resolve(principal);
        Page<IncomeResponse> page = incomeService.search(user, source, startDate, endDate, minAmount, maxAmount, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}
