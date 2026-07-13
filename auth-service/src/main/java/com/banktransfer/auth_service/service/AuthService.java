package com.banktransfer.auth_service.service;

import com.banktransfer.auth_service.dto.AuthResponse;
import com.banktransfer.auth_service.dto.LoginRequest;
import com.banktransfer.auth_service.dto.RegisterRequest;
import com.banktransfer.auth_service.mapper.UserMapper;
import com.banktransfer.auth_service.model.User;
import com.banktransfer.auth_service.Repository.UserRepository;
import com.banktransfer.auth_service.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(Map.of("userId", saved.getId()), saved);

        AuthResponse response = userMapper.toAuthResponse(saved);
        response.setToken(token);
        return response;
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        String token = jwtService.generateToken(Map.of("userId", user.getId()), user);

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setToken(token);
        return response;
    }
}