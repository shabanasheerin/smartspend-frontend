package com.smartspend.service;

import com.smartspend.dto.auth.*;
import com.smartspend.entity.RefreshToken;
import com.smartspend.entity.Role;
import com.smartspend.entity.User;
import com.smartspend.entity.VerificationToken;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.RoleRepository;
import com.smartspend.repository.UserRepository;
import com.smartspend.repository.VerificationTokenRepository;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long EMAIL_VERIFICATION_EXPIRY_MS = 24L * 60 * 60 * 1000;
    private static final long PASSWORD_RESET_EXPIRY_MS = 60L * 60 * 1000;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found. Please contact support."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        issueEmailVerificationToken(savedUser);

        return authenticate(new LoginRequest(savedUser.getEmail(), request.getPassword()));
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user), null, new CustomUserDetails(user).getAuthorities());

        String newAccessToken = jwtUtil.generateAccessToken(authentication);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(User user) {
        refreshTokenService.deleteByUser(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        notificationService.notifyPasswordChanged(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByTokenAndType(token, VerificationToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (verificationToken.isUsed() || verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired verification token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("This email is already verified");
        }

        issueEmailVerificationToken(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Deliberately does not throw if the email doesn't exist, to avoid
        // leaking which addresses have accounts.
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            verificationTokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);

            VerificationToken resetToken = VerificationToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .type(VerificationToken.TokenType.PASSWORD_RESET)
                    .expiryDate(Instant.now().plusMillis(PASSWORD_RESET_EXPIRY_MS))
                    .used(false)
                    .build();
            verificationTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken.getToken());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken resetToken = verificationTokenRepository
                .findByTokenAndType(request.getToken(), VerificationToken.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        verificationTokenRepository.save(resetToken);

        refreshTokenService.deleteByUser(user);
        notificationService.notifyPasswordChanged(user);
    }

    private void issueEmailVerificationToken(User user) {
        verificationTokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(Instant.now().plusMillis(EMAIL_VERIFICATION_EXPIRY_MS))
                .used(false)
                .build();
        verificationTokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token.getToken());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .currency(user.getCurrency())
                        .theme(user.getTheme().name())
                        .build())
                .build();
    }
}
