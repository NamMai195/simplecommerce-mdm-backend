package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.repository.EmailVerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-VERIFICATION-CLEANUP")
public class EmailVerificationTokenCleanupService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired email verification tokens");
        try {
            LocalDateTime now = LocalDateTime.now();
            emailVerificationTokenRepository.deleteExpiredTokens(now);
            log.info("Successfully cleaned up expired email verification tokens");
        } catch (Exception e) {
            log.error("Error during email verification token cleanup: {}", e.getMessage(), e);
        }
    }
}
