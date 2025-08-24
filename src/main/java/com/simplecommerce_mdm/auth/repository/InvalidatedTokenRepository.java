package com.simplecommerce_mdm.auth.repository;

import com.simplecommerce_mdm.auth.model.InvalidatedToken;
import com.simplecommerce_mdm.common.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {

    Optional<InvalidatedToken> findByToken(String token);

    @Query("SELECT it FROM InvalidatedToken it WHERE it.token = :token AND it.tokenType = :tokenType")
    Optional<InvalidatedToken> findByTokenAndType(@Param("token") String token,
            @Param("tokenType") TokenType tokenType);

    @Query("SELECT it FROM InvalidatedToken it WHERE it.userEmail = :email ORDER BY it.invalidatedAt DESC")
    java.util.List<InvalidatedToken> findByUserEmailOrderByInvalidatedAtDesc(@Param("email") String email);

    @Query("SELECT it FROM InvalidatedToken it WHERE it.expiresAt < :now")
    java.util.List<InvalidatedToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM InvalidatedToken it WHERE it.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(it) FROM InvalidatedToken it WHERE it.userEmail = :email AND it.expiresAt > :now")
    long countActiveInvalidatedTokensForEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}
