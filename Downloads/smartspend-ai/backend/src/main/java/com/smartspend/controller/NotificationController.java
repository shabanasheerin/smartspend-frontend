package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.common.PagedResponse;
import com.smartspend.dto.notification.NotificationResponse;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications: budget alerts, goal achievements, reminders")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserProvider userProvider;

    @GetMapping
    @Operation(summary = "List notifications for the current user, most recent first")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        User user = userProvider.resolve(principal);
        Page<NotificationResponse> page = notificationService.listForUser(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", notificationService.countUnread(user))));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@AuthenticationPrincipal CustomUserDetails principal,
                                                                       @PathVariable Long id) {
        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(notificationService.markRead(user, id)));
    }
}
