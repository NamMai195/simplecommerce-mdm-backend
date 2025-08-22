package com.simplecommerce_mdm.auth.repository;

import com.simplecommerce_mdm.auth.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.userEmail = :email AND evt.isUsed = false AND evt.expiresAt > :now ORDER BY evt.createdAt DESC")
    Optional<EmailVerificationToken> findValidTokenByEmail(@Param("email") String email,
            @Param("now") LocalDateTime now);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.otpCode = :otpCode AND evt.userEmail = :email AND evt.isUsed = false AND evt.expiresAt > :now")
    Optional<EmailVerificationToken> findByOtpCodeAndEmail(@Param("otpCode") String otpCode,
            @Param("email") String email, @Param("now") LocalDateTime now);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.userEmail = :email ORDER BY evt.createdAt DESC")
    java.util.List<EmailVerificationToken> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.expiresAt < :now")
    java.util.List<EmailVerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailVerificationToken evt SET evt.isUsed = true, evt.usedAt = :now WHERE evt.userEmail = :email AND evt.isUsed = false")
    void invalidateAllTokensForEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(evt) FROM EmailVerificationToken evt WHERE evt.userEmail = :email AND evt.isUsed = false AND evt.expiresAt > :now")
    long countActiveTokensForEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}
