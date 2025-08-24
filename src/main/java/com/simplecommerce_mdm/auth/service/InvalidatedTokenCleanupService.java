package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.repository.InvalidatedTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "INVALIDATED-TOKEN-CLEANUP")
public class InvalidatedTokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired invalidated tokens");
        try {
            LocalDateTime now = LocalDateTime.now();
            invalidatedTokenRepository.deleteExpiredTokens(now);
            log.info("Successfully cleaned up expired invalidated tokens");
        } catch (Exception e) {
            log.error("Error during invalidated token cleanup: {}", e.getMessage(), e);
        }
    }
}
