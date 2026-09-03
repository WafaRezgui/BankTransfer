package com.banktransfer.notification_service.client;

import com.banktransfer.notification_service.security.TechnicalAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class UserClient {

    @Value("${services.gateway-url}")
    private String gatewayUrl;

    private final TechnicalAuthService technicalAuthService;

    private WebClient webClient() {
        return WebClient.create(gatewayUrl);
    }

    public UserInfo getUserById(Long userId) {
        String token = technicalAuthService.getValidToken();

        return webClient()
                .get()
                .uri("/auth/users/{id}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}