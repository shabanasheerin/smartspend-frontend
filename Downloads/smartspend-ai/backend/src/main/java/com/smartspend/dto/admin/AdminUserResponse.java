package com.smartspend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean emailVerified;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
