package com.smartspend.service;

import com.smartspend.dto.admin.AdminStatsResponse;
import com.smartspend.dto.admin.AdminUserResponse;
import com.smartspend.dto.admin.AuditLogResponse;
import com.smartspend.entity.Role;
import com.smartspend.entity.User;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.AuditLogRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.IncomeRepository;
import com.smartspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AuditLogRepository auditLogRepository;

    public Page<AdminUserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Transactional
    public AdminUserResponse blockUser(Long userId) {
        User user = getUser(userId);
        if (user.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN)) {
            throw new BadRequestException("Cannot block an admin account");
        }
        user.setEnabled(false);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse unblockUser(Long userId) {
        User user = getUser(userId);
        user.setEnabled(true);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        if (user.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN)) {
            throw new BadRequestException("Cannot delete an admin account");
        }
        userRepository.delete(user);
    }

    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByEnabledTrue())
                .blockedUsers(userRepository.countByEnabledFalse())
                .totalIncomeRecords(incomeRepository.count())
                .totalExpenseRecords(expenseRepository.count())
                .totalIncomeAmountAllUsers(incomeRepository.sumAllAmounts())
                .totalExpenseAmountAllUsers(expenseRepository.sumAllAmounts())
                .build();
    }

    public Page<AuditLogResponse> listAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(log ->
                AuditLogResponse.builder()
                        .id(log.getId())
                        .userEmail(log.getUser() != null ? log.getUser().getEmail() : "SYSTEM")
                        .action(log.getAction())
                        .details(log.getDetails())
                        .ipAddress(log.getIpAddress())
                        .createdAt(log.getCreatedAt())
                        .build());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AdminUserResponse toUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
