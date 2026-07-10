package com.smartspend.controller;

import com.smartspend.dto.budget.BudgetRequest;
import com.smartspend.dto.budget.BudgetResponse;
import com.smartspend.dto.common.ApiResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Monthly category budgets with usage tracking")
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping
    @Operation(summary = "Create a monthly budget for a category")
    public ResponseEntity<ApiResponse<BudgetResponse>> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @Valid @RequestBody BudgetRequest request) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created", budgetService.create(user, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody BudgetRequest request) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success("Budget updated", budgetService.update(user, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        budgetService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted", null));
    }

    @GetMapping
    @Operation(summary = "List budgets for a given month/year with usage stats")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @RequestParam Integer month,
                                                                   @RequestParam Integer year) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(budgetService.listForMonth(user, month, year)));
    }
}
