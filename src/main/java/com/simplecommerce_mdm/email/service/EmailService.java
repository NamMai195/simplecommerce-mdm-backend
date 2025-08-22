package com.simplecommerce_mdm.email.service;

import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.order.model.Order;
import java.util.Map;

public interface EmailService {

    /**
     * Sends a simple text-based email.
     *
     * @param to          The recipient's email address.
     * @param subject     The subject of the email.
     * @param textContent The plain text content of the email.
     */
    void sendSimpleMessage(String to, String subject, String textContent);

    /**
     * Sends an email using a pre-defined Brevo template.
     *
     * @param to             The recipient's email address.
     * @param templateId     The ID of the template on the Brevo platform.
     * @param templateParams A map of parameters to populate the template.
     */
    void sendEmailWithTemplate(String to, Long templateId, Map<String, Object> templateParams);

    // === ORDER-RELATED EMAIL METHODS ===

    /**
     * Sends order confirmation email to buyer after successful order creation.
     * Uses Brevo template for order confirmation with order details, items, and
     * tracking info.
     *
     * @param masterOrder The master order containing all order details
     */
    void sendOrderConfirmationEmail(MasterOrder masterOrder);

    /**
     * Sends new order alert email to seller when they receive a new order.
     * Uses Brevo template to notify seller with order details and action required.
     *
     * @param order The individual order (per shop) that was created
     */
    void sendNewOrderAlertEmail(Order order);

    /**
     * Sends order status update email to buyer when order status changes.
     * Uses Brevo template with status-specific content and next steps.
     *
     * @param order     The order that had status change
     * @param oldStatus The previous status (for display in email)
     * @param newStatus The current status (for display in email)
     */
    void sendOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus);

    // === AUTH-RELATED EMAIL METHODS ===

    /**
     * Sends password reset OTP email to user.
     *
     * @param to            recipient email
     * @param otpCode       numeric OTP code
     * @param expireMinutes validity in minutes
     * @param userName      recipient display name
     */
    void sendPasswordResetOtpEmail(String to, String otpCode, int expireMinutes, String userName);

    /**
     * Sends email verification OTP email to user.
     *
     * @param to            recipient email
     * @param otpCode       numeric OTP code
     * @param expireMinutes validity in minutes
     * @param userName      recipient display name
     */
    void sendEmailVerificationOtpEmail(String to, String otpCode, int expireMinutes, String userName);
}