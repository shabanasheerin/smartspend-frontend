package com.smartspend.repository;

import com.smartspend.entity.User;
import com.smartspend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, VerificationToken.TokenType type);
    void deleteByUserAndType(User user, VerificationToken.TokenType type);
}
