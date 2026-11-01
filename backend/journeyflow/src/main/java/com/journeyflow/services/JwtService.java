package com.journeyflow.services;

import java.time.Clock;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final int MIN_KEY_BYTES = 32; // HS256 exige no mínimo 256 bits

    private final SecretKey signingKey;
    private final long expirationMs;
    private final Clock clock;

    /**
     * A chave é decodificada e validada uma única vez, no startup: se security.jwt.secret estiver
     * vazio ou fraco a aplicação falha ao subir, em vez de falhar só no primeiro login.
     */
    public JwtService(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs,
            Clock clock) {

        this.signingKey = buildKey(secret);
        this.expirationMs = expirationMs;
        this.clock = clock;
    }

    public String generateToken(String id, String email, String role) {
        Date now = Date.from(clock.instant());
        Date expiresAt = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    /** Lança JwtException se o token for inválido, adulterado ou expirado. */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "security.jwt.secret não configurado (defina SECURITY_JWT_SECRET em Base64, mínimo 256 bits)");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("security.jwt.secret deve estar codificado em Base64", ex);
        }

        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("security.jwt.secret deve ter no mínimo 256 bits (32 bytes)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
