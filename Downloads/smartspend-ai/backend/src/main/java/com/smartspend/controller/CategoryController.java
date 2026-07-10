package com.smartspend.controller;

import com.smartspend.dto.category.CategoryResponse;
import com.smartspend.dto.common.ApiResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "System-defined and user-defined expense/income categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthenticatedUserProvider userProvider;

    @GetMapping
    @Operation(summary = "List all categories available to the current user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(categoryService.listForUser(user)));
    }
}
