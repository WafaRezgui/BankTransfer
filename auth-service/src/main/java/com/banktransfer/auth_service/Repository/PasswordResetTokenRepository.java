package com.banktransfer.auth_service.Repository;

import com.banktransfer.auth_service.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(Long userId);   // pour invalider les anciens tokens quand on en génère un nouveau
}