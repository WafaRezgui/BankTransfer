package com.banktransfer.gateway_service.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    // Routes publiques : accessibles SANS token
    private final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Laisse passer directement les routes publiques
        if (isOpenEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Vérifie la présence du header Authorization
        if (!request.getHeaders().containsKey("Authorization")) {
            return onError(exchange, "Header Authorization manquant");
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Format du token invalide");
        }

        String token = authHeader.substring(7);

        try {
            validateToken(token);
        } catch (Exception e) {
            return onError(exchange, "Token invalide ou expiré : " + e.getMessage());
        }

        return chain.filter(exchange);
    }

    private boolean isOpenEndpoint(String path) {
        return openApiEndpoints.stream().anyMatch(path::startsWith);
    }

    private void validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);   // lève une exception si invalide/expiré
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;   // s'exécute avant les autres filtres du Gateway
    }


}