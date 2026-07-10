package com.smartspend.service;

import com.smartspend.dto.notification.NotificationResponse;
import com.smartspend.entity.Notification;
import com.smartspend.entity.User;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles creation of in-app notifications.
 * Budget alerts, goal-achieved, and recurring-reminder triggers call into
 * this service from BudgetService, GoalService, and the recurring-expense scheduler.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyPasswordChanged(User user) {
        create(user, "Password Changed", "Your account password was changed successfully.",
                Notification.NotificationType.PASSWORD_CHANGED);
    }

    @Transactional
    public Notification create(User user, String title, String message, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public Page<NotificationResponse> listForUser(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable).map(this::toResponse);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public NotificationResponse markRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

