package com.simplecommerce_mdm.email.events;

import com.simplecommerce_mdm.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEmailEventListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmation(OrderEmailEvents.OrderConfirmationEvent event) {
        try {
            emailService.sendOrderConfirmationEmail(event.getMasterOrder());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send order confirmation: {}", e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewOrderAlert(OrderEmailEvents.NewOrderAlertEvent event) {
        try {
            emailService.sendNewOrderAlertEmail(event.getOrder());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send new order alert: {}", e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusUpdate(OrderEmailEvents.OrderStatusUpdateEvent event) {
        try {
            emailService.sendOrderStatusUpdateEmail(event.getOrder(), event.getOldStatus(), event.getNewStatus());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send status update: {}", e.getMessage(), e);
        }
    }
} 