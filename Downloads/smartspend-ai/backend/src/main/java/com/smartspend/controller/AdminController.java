package com.smartspend.controller;

import com.smartspend.dto.admin.AdminStatsResponse;
import com.smartspend.dto.admin.AdminUserResponse;
import com.smartspend.dto.admin.AuditLogResponse;
import com.smartspend.dto.category.CategoryResponse;
import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.common.PagedResponse;
import com.smartspend.entity.Category;
import com.smartspend.mapper.CategoryMapper;
import com.smartspend.repository.CategoryRepository;
import com.smartspend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * All endpoints in this controller require ROLE_ADMIN.
 * Route-level protection is also enforced in SecurityConfig ("/api/v1/admin/**").
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Platform administration: users, stats, categories, audit logs")
public class AdminController {

    private final AdminService adminService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminUserResponse> page = adminService.listUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PatchMapping("/users/{id}/block")
    @Operation(summary = "Block a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User blocked", adminService.blockUser(id)));
    }

    @PatchMapping("/users/{id}/unblock")
    @Operation(summary = "Unblock a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User unblocked", adminService.unblockUser(id)));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide statistics")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "List audit logs (paginated, most recent first)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> auditLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> page = adminService.listAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new global (system-defined) category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .type(request.type())
                .systemDefined(true)
                .build();
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", categoryMapper.toResponse(saved)));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete a global category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    public record CreateCategoryRequest(@NotBlank String name, Category.CategoryType type) {
    }
}
