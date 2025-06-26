package com.simplecommerce_mdm.email.service.impl;

import com.simplecommerce_mdm.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * A mock implementation of the EmailService that is only active during tests.
 * It does not send real emails but logs the actions instead.
 * This prevents side effects and saves costs during automated testing.
 */
@Service
@Slf4j
@Profile("test")
public class MockEmailService implements EmailService {

    @Override
    public void sendSimpleMessage(String to, String subject, String textContent) {
        log.info("[MOCK EMAIL] Sending simple message to: {}", to);
        log.info("[MOCK EMAIL] Subject: {}", subject);
        log.info("[MOCK EMAIL] Content: {}", textContent);
    }

    @Override
    public void sendEmailWithTemplate(String to, Long templateId, Map<String, Object> templateParams) {
        log.info("[MOCK EMAIL] Sending template email to: {}", to);
        log.info("[MOCK EMAIL] Template ID: {}", templateId);
        log.info("[MOCK EMAIL] Params: {}", templateParams);
    }
} 