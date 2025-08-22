package com.simplecommerce_mdm.email.constants;

/**
 * Constants for Brevo email template IDs
 * These IDs correspond to templates created in Brevo dashboard
 */
public class EmailTemplateConstants {

    // Authentication & User Management Templates
    public static final Long WELCOME_EMAIL_TEMPLATE_ID = 8L;
    public static final Long PASSWORD_RESET_OTP_TEMPLATE_ID = 12L;
    public static final Long EMAIL_VERIFICATION_OTP_TEMPLATE_ID = 13L;

    // Order Management Templates
    public static final Long ORDER_CONFIRMATION_TEMPLATE_ID = 9L; // Buyer: Order Confirmation
    public static final Long NEW_ORDER_ALERT_TEMPLATE_ID = 10L; // Seller: New Order Alert
    public static final Long ORDER_STATUS_UPDATE_TEMPLATE_ID = 11L; // Buyer: Order Status Update

    // Private constructor to prevent instantiation
    private EmailTemplateConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}