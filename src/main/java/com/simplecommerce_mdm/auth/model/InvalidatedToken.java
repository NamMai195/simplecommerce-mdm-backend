package com.simplecommerce_mdm.auth.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.common.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invalidated_tokens", indexes = {
        @Index(name = "idx_invalidated_token", columnList = "token", unique = true),
        @Index(name = "idx_invalidated_user_email", columnList = "user_email"),
        @Index(name = "idx_invalidated_expires", columnList = "expires_at")
})
public class InvalidatedToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "token_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "invalidated_at", nullable = false)
    private LocalDateTime invalidatedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
