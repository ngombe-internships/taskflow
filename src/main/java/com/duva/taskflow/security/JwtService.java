package com.duva.taskflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service JWT professionnel et simple
 *
 * Standards appliqués:
 * - HS256 (HMAC-SHA256) - Suffisant pour la plupart des cas
 * - Claims enrichis (role, type de token)
 * - Validation stricte
 * - Distinction Access vs Refresh token
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer:taskflow-app}")
    private String issuer;

    private Key signKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build();
    }

    // TOKEN GENERATION

    /**
     * Génère un Access Token
     * - Contient: email, role, type, iat, exp
     * - Expiration: courte (~15 min)
     * - Usage: Autoriser les requêtes API
     */
    public String generateAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ACCESS");
        claims.put("role", role);

        return buildToken(email, claims, accessExpiration);
    }

    /**
     * Génère un Refresh Token
     * - Contient: email, type, iat, exp
     * - Expiration: longue (~7 jours)
     * - Pas de role/permissions (sécurité)
     * - Usage: Obtenir un nouveau access token
     */
    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        return buildToken(email, claims, refreshExpiration);
    }

    /**
     * Construction interne du token
     * Standards JWT appliqués:
     * - sub (subject) = email
     * - iss (issuer) = identité du serveur
     * - iat (issued at) = quand créé
     * - exp (expiration) = quand expire
     * - type = ACCESS ou REFRESH
     * - role = permission de l'utilisateur
     */
    private String buildToken(String email, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                // Standard claims
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Custom claims
                .addClaims(claims)
                // Signature
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // CLAIM EXTRACTION

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }

    // VALIDATION

    /**
     * Valide un token Access
     * - Vérifie la signature
     * - Vérifie l'expiration
     * - Vérifie que c'est bien un ACCESS token
     * - Vérifie que l'email correspond
     */
    public boolean isAccessTokenValid(String token, String email) {
        try {
            String tokenType = extractTokenType(token);
            String extractedEmail = extractEmail(token);

            return "ACCESS".equals(tokenType)
                    && extractedEmail.equals(email)
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valide un token Refresh
     * - Vérifie la signature
     * - Vérifie l'expiration
     * - Vérifie que c'est bien un REFRESH token
     * - Vérifie que l'email correspond
     */
    public boolean isRefreshTokenValid(String token, String email) {
        try {
            String tokenType = extractTokenType(token);
            String extractedEmail = extractEmail(token);

            return "REFRESH".equals(tokenType)
                    && extractedEmail.equals(email)
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}