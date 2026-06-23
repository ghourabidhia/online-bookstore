package com.bnp.bookstore.security;


import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;

import java.util.Date;


/**
 * Creates, reads, and checks JWT login tokens.
 * The secret key and expiry time come from application.properties.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }


    /**
     * Create a new JWT token for a user.
     * The token stores the user's email and has an expiry date.
     */
    public String generateToken(UserDetails user) {
        log.debug("Generating JWT for user: {}", user.getUsername());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }


    /**
     * Read the email address stored inside a token.
     * Throws an error if the token is invalid or tampered with.
     */
    public String extractUsername(String token) {
        log.debug("Extracting username from JWT token");

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    /**
     * Check if a token belongs to this user and hasn't expired.
     * Returns true only if both checks pass.
     */
    public boolean isValid(String token, UserDetails user) {
        String email = extractUsername(token);
        boolean valid = email.equals(user.getUsername()) && !isExpired(token);

        if (valid) {
            log.debug("JWT valid for user: {}", email);
        } else {
            log.warn("JWT invalid or expired for user: {}", email);
        }

        return valid;
    }


    /** Check if the token's expiry date has already passed. */
    private boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }


}
