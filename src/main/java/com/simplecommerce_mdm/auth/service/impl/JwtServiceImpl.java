package com.simplecommerce_mdm.auth.service.impl;

import com.simplecommerce_mdm.auth.service.JwtService;
import com.simplecommerce_mdm.common.enums.TokenType;
import com.simplecommerce_mdm.exception.InvalidDataException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.simplecommerce_mdm.common.enums.TokenType.ACCESS_TOKEN;
import static com.simplecommerce_mdm.common.enums.TokenType.REFRESH_TOKEN;

@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expiryMinutes}")
    private long expiryMinutes;

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Override
    public String generateAccessToken(long userId, String username,
            Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate access token for user {} (email: {}) with authorities {}", userId, username, authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);

        return generateToken(claims, username);
    }

    @Override
    public String generateRefreshToken(long userId, String username,
            Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate refresh token for user {} (email: {})", userId, username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);

        return generateRefreshToken(claims, username);
    }

    @Override
    public String extractEmail(String token, TokenType type) {
        return extractClaim(token, type, Claims::getSubject);
    }

    private String generateToken(Map<String, Object> claims, String subject) {
        log.info("----------[ generateToken ]----------");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Sử dụng subject (là email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutes))
                .signWith(getKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String subject) {
        log.info("----------[ generateRefreshToken ]----------");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay))
                .signWith(getKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType type) {
        log.info("----------[ getKey ]----------");
        switch (type) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            default -> throw new InvalidDataException("Invalid token type");
        }
    }

    private <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimResolver) {
        log.info("----------[ extractClaim ]----------");
        final Claims claims = extraAllClaim(token, type);
        return claimResolver.apply(claims);
    }

    private Claims extraAllClaim(String token, TokenType type) {
        log.info("----------[ extraAllClaim ]----------");
        try {
            return Jwts.parserBuilder().setSigningKey(getKey(type)).build().parseClaimsJws(token).getBody();
        } catch (SignatureException | ExpiredJwtException e) {
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }
    }

    @Override
    public Long extractUserId(String token) {
        log.info("----------[ extractUserId ]----------");
        try {
            Claims claims = extraAllClaim(token, TokenType.ACCESS_TOKEN);
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                log.error("Token does not contain userId");
                throw new AccessDeniedException("Token không hợp lệ: không tìm thấy userId");
            }
            return Long.valueOf(userIdObj.toString());
        } catch (NumberFormatException e) {
            log.error("Error converting userId: {}", e.getMessage());
            throw new AccessDeniedException("Token không hợp lệ: userId không đúng định dạng");
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage());
            throw new AccessDeniedException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Override
    public LocalDateTime extractExpiration(String token) {
        log.info("----------[ extractExpiration ]----------");
        try {
            Claims claims = extraAllClaim(token, TokenType.ACCESS_TOKEN);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                log.error("Token does not contain expiration");
                throw new AccessDeniedException("Token không hợp lệ: không tìm thấy thời gian hết hạn");
            }
            return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            log.error("Error extracting expiration from token: {}", e.getMessage());
            throw new AccessDeniedException("Token không hợp lệ hoặc đã hết hạn");
        }
    }
}