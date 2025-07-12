package com.moddynerd.apigateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class AuthTokenFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        // Convert hex string to byte array
        byte[] keyBytes = new java.math.BigInteger(secret, 16).toByteArray();
        // Remove leading zero byte if present (BigInteger quirk)
        if (keyBytes.length > 32 && keyBytes[0] == 0) {
            byte[] tmp = new byte[keyBytes.length - 1];
            System.arraycopy(keyBytes, 1, tmp, 0, tmp.length);
            keyBytes = tmp;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        // Skip token check for login and registration paths
        if (path.startsWith("/user/login") || path.startsWith("/user/register") || path.endsWith(".m3u8") ||
                path.endsWith(".ts")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !validateJwt(authHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    public String extractUserId(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().get("userId", String.class);
    }

    public boolean isValidToken(String token, String userId) {
        return extractUserId(token).equals(userId) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }

    private boolean validateJwt(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
