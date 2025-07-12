package com.moddynerd.userservice.Utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

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

    public String generateToken(String userId){
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().get("userId", String.class);
    }

    public boolean validateToken(String token, String userId) {
        return extractUserId(token).equals(userId) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
}
