package com.banktransfer.notification_service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class TechnicalAuthService {

    @Value("${services.gateway-url}")
    private String gatewayUrl;

    @Value("${services.notification-technical-user.email}")
    private String email;

    @Value("${services.notification-technical-user.password}")
    private String password;

    private String cachedToken;
    private Instant tokenExpiry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public synchronized String getValidToken() {
        // Si on a déjà un token, et qu'il n'expire pas dans moins de 60 secondes, on le réutilise
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry.minusSeconds(60))) {
            return cachedToken;
        }

        log.info("Authentification technique de notification-service auprès d'auth-service...");

        Map<String, Object> response = WebClient.create(gatewayUrl)
                .post()
                .uri("/auth/login")
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("token") == null) {
            throw new IllegalStateException("Impossible d'obtenir un token technique pour notification-service");
        }

        cachedToken = (String) response.get("token");
        tokenExpiry = extractExpiry(cachedToken);

        log.info("Token technique obtenu, valide jusqu'à {}", tokenExpiry);
        return cachedToken;
    }

    // Décode le JWT (sans vérifier la signature, juste pour lire sa date d'expiration)
    private Instant extractExpiry(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode node = objectMapper.readTree(payload);
            long expSeconds = node.get("exp").asLong();
            return Instant.ofEpochSecond(expSeconds);
        } catch (Exception e) {
            log.warn("Impossible de décoder l'expiration du token, valeur par défaut appliquée");
            return Instant.now().plusSeconds(3600);   // fallback prudent : 1h
        }
    }
}