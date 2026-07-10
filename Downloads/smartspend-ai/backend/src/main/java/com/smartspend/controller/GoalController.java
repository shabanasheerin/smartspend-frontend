package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.goal.GoalContributionRequest;
import com.smartspend.dto.goal.GoalRequest;
import com.smartspend.dto.goal.GoalResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.GoalService;
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
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Create and track progress toward savings goals")
public class GoalController {

    private final GoalService goalService;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<ApiResponse<GoalResponse>> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                             @Valid @RequestBody GoalRequest request) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created", goalService.create(user, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a savings goal")
    public ResponseEntity<ApiResponse<GoalResponse>> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody GoalRequest request) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success("Goal updated", goalService.update(user, id, request)));
    }

    @PostMapping("/{id}/contribute")
    @Operation(summary = "Add a contribution toward a savings goal")
    public ResponseEntity<ApiResponse<GoalResponse>> contribute(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @PathVariable Long id,
                                                                 @Valid @RequestBody GoalContributionRequest request) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success("Contribution added", goalService.contribute(user, id, request)));
    }

    @PostMapping("/{id}/abandon")
    @Operation(summary = "Mark a savings goal as abandoned")
    public ResponseEntity<ApiResponse<GoalResponse>> abandon(@AuthenticationPrincipal CustomUserDetails principal,
                                                              @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success("Goal abandoned", goalService.abandon(user, id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        goalService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted", null));
    }

    @GetMapping
    @Operation(summary = "List all savings goals for the current user")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(goalService.listForUser(user)));
    }
}
