package com.banktransfer.auth_service.service;

import com.banktransfer.auth_service.Repository.UserRepository;
import com.banktransfer.auth_service.dto.AuthResponse;
import com.banktransfer.auth_service.dto.ForgotPasswordRequest;
import com.banktransfer.auth_service.dto.LoginRequest;
import com.banktransfer.auth_service.dto.RegisterRequest;
import com.banktransfer.auth_service.dto.ResetPasswordRequest;
import com.banktransfer.auth_service.dto.UserResponse;
import com.banktransfer.auth_service.event.PasswordResetRequestedEvent;
import com.banktransfer.auth_service.kafka.PasswordResetEventProducer;
import com.banktransfer.auth_service.mapper.UserMapper;
import com.banktransfer.auth_service.model.PasswordResetToken;
import com.banktransfer.auth_service.model.Role;
import com.banktransfer.auth_service.model.User;
import com.banktransfer.auth_service.Repository.PasswordResetTokenRepository;
import com.banktransfer.auth_service.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEventProducer passwordResetEventProducer;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);   // ← forcé, quoi que le client envoie dans la requête

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(Map.of("userId", saved.getId()), saved);

        AuthResponse response = userMapper.toAuthResponse(saved);
        response.setToken(token);
        return response;
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        try {

            // Tentative d'authentification
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // ============================
            // LOGIN RÉUSSI
            // ============================

            // On remet le compteur à zéro
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            String token = jwtService.generateToken(
                    Map.of("userId", user.getId()),
                    user
            );

            AuthResponse response = userMapper.toAuthResponse(user);
            response.setToken(token);
            response.setAdditionalVerificationRequired(false);

            return response;

        } catch (Exception e) {

            // ============================
            // LOGIN ÉCHOUÉ
            // ============================

            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            userRepository.save(user);

            System.out.println(
                    "Tentative de connexion échouée pour "
                            + user.getEmail()
                            + " - tentative n°"
                            + user.getFailedLoginAttempts()
            );

            // À partir de 3 tentatives :
            if (user.getFailedLoginAttempts() >= 3) {

                AuthResponse response = AuthResponse.builder()
                        .email(user.getEmail())
                        .userId(user.getId())
                        .additionalVerificationRequired(true)
                        .build();

                return response;
            }

            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public UserResponse updateUserRole(Long id, Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Le rôle fourni est invalide");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setRole(newRole);
        User updated = userRepository.save(user);

        return UserResponse.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .firstName(updated.getFirstName())
                .lastName(updated.getLastName())
                .build();
    }
    @Transactional

    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte associé à cet email"));

        // On invalide tout ancien token en cours pour cet utilisateur
        // (évite d'avoir plusieurs liens de reset valides en même temps)
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        passwordResetEventProducer.publishPasswordResetRequested(
                PasswordResetRequestedEvent.builder()
                        .userId(user.getId())
                        .token(token)
                        .build()
        );
    }

    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Ce lien de réinitialisation a déjà été utilisé");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce lien de réinitialisation a expiré");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}