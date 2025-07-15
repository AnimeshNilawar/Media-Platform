package com.moddynerd.apigateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class AuthTokenFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = new java.math.BigInteger(secret, 16).toByteArray();
        if (keyBytes.length > 32 && keyBytes[0] == 0) {
            byte[] tmp = new byte[keyBytes.length - 1];
            System.arraycopy(keyBytes, 1, tmp, 0, tmp.length);
            keyBytes = tmp;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/user/login") || path.startsWith("/user/register")
                || path.endsWith(".m3u8") || path.endsWith(".ts")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !validateJwt(authHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        String userId = extractUserId(token);

        // ✅ Correct way to set custom header
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(builder -> builder.header("X-User-Id", userId))
                .build();

        return chain.filter(modifiedExchange);
    }


    public String extractUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValidToken(String token, String userId) {
        return extractUserId(token).equals(userId) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }

    private boolean validateJwt(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return -1; // Ensure it runs early
    }
}
