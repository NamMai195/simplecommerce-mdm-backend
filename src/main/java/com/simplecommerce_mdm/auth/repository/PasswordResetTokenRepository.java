package com.simplecommerce_mdm.auth.repository;

import com.simplecommerce_mdm.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find valid token by user email
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.userEmail = :email AND prt.isUsed = false AND prt.expiresAt > :now ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findValidTokenByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * Find token by OTP code and user email
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.otpCode = :otpCode AND prt.userEmail = :email AND prt.isUsed = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findByOtpCodeAndEmail(@Param("otpCode") String otpCode, @Param("email") String email,
            @Param("now") LocalDateTime now);

    /**
     * Find all tokens for a user email
     */
    List<PasswordResetToken> findByUserEmailOrderByCreatedAtDesc(String email);

    /**
     * Find expired tokens
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.expiresAt < :now AND prt.isUsed = false")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for a user email
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.isUsed = true, prt.usedAt = :now WHERE prt.userEmail = :email AND prt.isUsed = false")
    void invalidateAllTokensForEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * Count active tokens for a user email
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.userEmail = :email AND prt.isUsed = false AND prt.expiresAt > :now")
    long countActiveTokensForEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}