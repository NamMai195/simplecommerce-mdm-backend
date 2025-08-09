package com.simplecommerce_mdm.email.service.impl;

import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.order.model.Order;
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

    @Override
    public void sendOrderConfirmationEmail(MasterOrder masterOrder) {
        log.info("[MOCK EMAIL] Sending order confirmation email for order: {} to: {}", 
            masterOrder.getOrderGroupNumber(), masterOrder.getUser().getEmail());
    }

    @Override
    public void sendNewOrderAlertEmail(Order order) {
        log.info("[MOCK EMAIL] Sending new order alert email for order: {} to seller: {}", 
            order.getOrderNumber(), order.getShop().getUser().getEmail());
    }

    @Override
    public void sendOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus) {
        log.info("[MOCK EMAIL] Sending order status update email for order: {} from {} to {} to customer: {}", 
            order.getOrderNumber(), oldStatus, newStatus, order.getMasterOrder().getUser().getEmail());
    }

    @Override
    public void sendPasswordResetOtpEmail(String to, String otpCode, int expireMinutes, String userName) {
        log.info("[MOCK EMAIL] Sending password reset OTP to {} - user: {}, otp: {}, expire: {}m", 
            to, userName, otpCode, expireMinutes);
    }
} 