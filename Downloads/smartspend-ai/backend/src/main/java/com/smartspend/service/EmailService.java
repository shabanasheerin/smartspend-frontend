package com.smartspend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around JavaMailSender. Failures are logged rather than thrown
 * so that registration/password-reset flows don't hard-fail a user-facing
 * request just because outbound mail is temporarily unavailable — the token
 * itself is still created and valid, it just wasn't emailed successfully.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String subject = "Verify your SmartSpend AI account";
        String body = "Hi " + fullName + ",\n\n"
                + "Please verify your email address by visiting the link below:\n"
                + link + "\n\n"
                + "This link expires in 24 hours.\n\n"
                + "If you didn't create a SmartSpend AI account, you can ignore this email.";
        send(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String subject = "Reset your SmartSpend AI password";
        String body = "Hi " + fullName + ",\n\n"
                + "We received a request to reset your password. Visit the link below to choose a new one:\n"
                + link + "\n\n"
                + "This link expires in 1 hour. If you didn't request this, you can ignore this email.";
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
