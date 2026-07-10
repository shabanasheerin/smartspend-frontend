package com.smartspend.service;

import com.smartspend.dto.auth.LoginRequest;
import com.smartspend.dto.auth.RegisterRequest;
import com.smartspend.entity.Role;
import com.smartspend.entity.User;
import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.RoleRepository;
import com.smartspend.repository.UserRepository;
import com.smartspend.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartspend.security.JwtUtil;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Test User", "test@example.com", "SecurePass123");
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_throwsResourceNotFoundException_whenDefaultRoleMissing() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.register(registerRequest));
    }

    @Test
    void resendVerificationEmail_throwsResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.resendVerificationEmail("missing@example.com"));
    }

    @Test
    void forgotPassword_doesNothing_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // Should not throw — deliberately silent to avoid leaking which emails have
        // accounts.
        authService.forgotPassword(new com.smartspend.dto.auth.ForgotPasswordRequest("missing@example.com"));

        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }
}
