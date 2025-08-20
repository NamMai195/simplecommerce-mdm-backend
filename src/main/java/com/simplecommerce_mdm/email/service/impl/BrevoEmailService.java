package com.simplecommerce_mdm.email.service.impl;

import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.exception.EmailSendingException;
import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.order.model.OrderItem;
import com.simplecommerce_mdm.order.repository.OrderItemRepository;
import com.simplecommerce_mdm.order.repository.OrderRepository;
import com.simplecommerce_mdm.user.model.Address;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import static com.simplecommerce_mdm.email.constants.EmailTemplateConstants.*;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrevoEmailService implements EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final CloudinaryService cloudinaryService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private TransactionalEmailsApi apiInstance;

    @PostConstruct
    public void init() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);
        apiInstance = new TransactionalEmailsApi(defaultClient);
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String textContent) {
        SendSmtpEmail sendSmtpEmail = createBaseEmail(to);
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setTextContent(textContent);

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Email sent successfully to {}. Message ID: {}", to, result.getMessageId());
        } catch (ApiException e) {
            log.error("Error sending email to {}: {}", to, e.getResponseBody(), e);
            throw new EmailSendingException("Failed to send simple email to " + to, e);
        }
    }

    @Override
    public void sendEmailWithTemplate(String to, Long templateId, Map<String, Object> templateParams) {
        SendSmtpEmail sendSmtpEmail = createBaseEmail(to);
        sendSmtpEmail.setTemplateId(templateId);

        if (templateParams != null && !templateParams.isEmpty()) {
            Properties params = new Properties();
            params.putAll(templateParams);
            sendSmtpEmail.setParams(params);
        }

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Template email sent successfully to {}. Message ID: {}", to, result.getMessageId());
        } catch (ApiException e) {
            log.error("Error sending template email to {}: {}", to, e.getResponseBody(), e);
            throw new EmailSendingException("Failed to send template email to " + to, e);
        }
    }

    // === ORDER-RELATED EMAIL IMPLEMENTATIONS ===

    @Override
    public void sendOrderConfirmationEmail(MasterOrder masterOrder) {
        try {
            Map<String, Object> templateParams = buildOrderConfirmationParams(masterOrder);
            sendEmailWithTemplate(
                    masterOrder.getUser().getEmail(),
                    ORDER_CONFIRMATION_TEMPLATE_ID,
                    templateParams);
            log.info("Order confirmation email sent for order: {}", masterOrder.getOrderGroupNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", masterOrder.getOrderGroupNumber(), e);
        }
    }

    @Override
    public void sendNewOrderAlertEmail(Order order) {
        try {
            Map<String, Object> templateParams = buildNewOrderAlertParams(order);
            sendEmailWithTemplate(
                    order.getShop().getUser().getEmail(),
                    NEW_ORDER_ALERT_TEMPLATE_ID,
                    templateParams);
            log.info("New order alert email sent for order: {} to seller: {}",
                    order.getOrderNumber(), order.getShop().getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send new order alert email for order: {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus) {
        try {
            Map<String, Object> templateParams = buildOrderStatusUpdateParams(order, oldStatus, newStatus);
            sendEmailWithTemplate(
                    order.getMasterOrder().getUser().getEmail(),
                    ORDER_STATUS_UPDATE_TEMPLATE_ID,
                    templateParams);
            log.info("Order status update email sent for order: {} from {} to {}",
                    order.getOrderNumber(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order: {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void sendPasswordResetOtpEmail(String to, String otpCode, int expireMinutes, String userName) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("brandName", "SimpleCommerce MDM");
            params.put("userName", userName);
            params.put("otpCode", otpCode);
            params.put("expireMinutes", expireMinutes);
            params.put("logoUrl", buildLogoUrl());
            params.put("supportUrl", buildSupportUrl());
            params.put("websiteUrl", "https://mdm-store.com");
            params.put("privacyUrl", "https://mdm-store.com/privacy");
            params.put("termsUrl", "https://mdm-store.com/terms");
            params.put("unsubscribeUrl", "https://mdm-store.com/unsubscribe");
            params.put("facebookUrl", "https://facebook.com/simplecommerce.mdm");
            params.put("instagramUrl", "https://instagram.com/simplecommerce.mdm");
            params.put("tiktokUrl", "https://tiktok.com/@simplecommerce.mdm");
            params.put("currentYear", String.valueOf(java.time.Year.now().getValue()));

            sendEmailWithTemplate(to, PASSWORD_RESET_OTP_TEMPLATE_ID, params);
            log.info("Password reset OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to {}: {}", to, e.getMessage(), e);
        }
    }

    // === TEMPLATE PARAMETER BUILDERS ===

    private Map<String, Object> buildOrderConfirmationParams(MasterOrder masterOrder) {
        Map<String, Object> params = new HashMap<>();

        // Basic order info
        params.put("customerName", masterOrder.getUser().getFullName());
        params.put("orderNumber", masterOrder.getOrderGroupNumber());
        params.put("orderDate", formatDateTime(masterOrder.getCreatedAt()));
        params.put("totalAmount", formatCurrency(masterOrder.getTotalAmountPaid()));
        params.put("paymentMethod", "Thanh toán khi nhận hàng (COD)");
        params.put("estimatedDelivery", calculateEstimatedDelivery());

        // Shipping address - parse from snapshot since there's no direct relation
        params.put("shippingStreet",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "street")
                        : "");
        params.put("shippingWard",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "ward")
                        : "");
        params.put("shippingDistrict",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "district")
                        : "");
        params.put("shippingCity",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "city")
                        : "");
        params.put("shippingPhone", masterOrder.getCustomerPhone() != null ? masterOrder.getCustomerPhone() : "");

        // Order items - explicitly load to avoid LazyInitialization in async AFTER_COMMIT
        List<Map<String, Object>> orderItems = new ArrayList<>();
        // Eagerly fetch shop to avoid LazyInitialization in async thread
        List<Order> orders = orderRepository.findByMasterOrderIdFetchShop(masterOrder.getId());
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("productName", item.getProductNameSnapshot() != null ? item.getProductNameSnapshot()
                        : (item.getVariant() != null ? item.getVariant().getProduct().getName() : "Unknown Product"));
                itemData.put("variantOptions",
                        item.getVariantOptionsSnapshot() != null ? item.getVariantOptionsSnapshot()
                                : (item.getVariant() != null && item.getVariant().getOptions() != null
                                        ? item.getVariant().getOptions()
                                        : ""));
                itemData.put("shopName", order.getShop().getName());
                itemData.put("quantity", item.getQuantity().toString());
                itemData.put("unitPrice", formatCurrency(item.getUnitPrice()));
                itemData.put("imageUrl", getProductImageUrlSafe(item));
                orderItems.add(itemData);
            }
        }
        params.put("orderItems", orderItems);

        // URLs and footer links
        addCommonEmailData(params);
        params.put("trackingUrl", buildTrackingUrl(masterOrder.getOrderGroupNumber()));

        return params;
    }

    private Map<String, Object> buildNewOrderAlertParams(Order order) {
        Map<String, Object> params = new HashMap<>();

        // Calculate order value
        BigDecimal orderValue = order.getSubtotalAmount()
                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

        // Seller and order info
        params.put("sellerName", order.getShop().getUser().getFullName());
        params.put("orderNumber", order.getOrderNumber());
        params.put("orderValue", formatCurrency(orderValue));
        params.put("orderDate", formatDateTime(order.getCreatedAt()));
        params.put("confirmationDeadline", calculateConfirmationDeadline(order.getCreatedAt()));

        // Customer info - get from master order
        MasterOrder masterOrder = order.getMasterOrder();
        params.put("customerName", masterOrder.getUser().getFullName());
        params.put("customerEmail", masterOrder.getUser().getEmail());
        params.put("customerPhone", masterOrder.getCustomerPhone() != null ? masterOrder.getCustomerPhone() : "");

        // Shipping address - parse from master order snapshot
        params.put("shippingStreet",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "street")
                        : "");
        params.put("shippingWard",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "ward")
                        : "");
        params.put("shippingDistrict",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "district")
                        : "");
        params.put("shippingCity",
                masterOrder.getShippingAddressSnapshot() != null
                        ? parseAddressField(masterOrder.getShippingAddressSnapshot(), "city")
                        : "");
        params.put("shippingPhone", masterOrder.getCustomerPhone() != null ? masterOrder.getCustomerPhone() : "");

        // Order items - explicitly load via repository to avoid LazyInitialization
        List<Map<String, Object>> orderItems = new ArrayList<>();
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productName", item.getProductNameSnapshot() != null ? item.getProductNameSnapshot()
                    : (item.getVariant() != null ? item.getVariant().getProduct().getName() : ""));
            itemData.put("sku", item.getVariant() != null && item.getVariant().getSku() != null ? item.getVariant().getSku() : "");
            itemData.put("variantOptions",
                    item.getVariantOptionsSnapshot() != null ? item.getVariantOptionsSnapshot()
                            : (item.getVariant() != null && item.getVariant().getOptions() != null ? item.getVariant().getOptions() : ""));
            itemData.put("quantity", item.getQuantity().toString());
            itemData.put("unitPrice", formatCurrency(item.getUnitPrice()));
            itemData.put("imageUrl", getProductImageUrlSafe(item));
            orderItems.add(itemData);
        }
        params.put("orderItems", orderItems);

        // Action URLs
        params.put("sellerDashboardUrl", buildSellerDashboardUrl());
        params.put("orderDetailsUrl", buildOrderDetailsUrl(order.getOrderNumber()));

        // Common data
        addCommonEmailData(params);

        return params;
    }

    private Map<String, Object> buildOrderStatusUpdateParams(Order order, String oldStatus, String newStatus) {
        Map<String, Object> params = new HashMap<>();

        // Calculate order value
        BigDecimal orderValue = order.getSubtotalAmount()
                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

        // Basic info - get customer from master order
        MasterOrder masterOrder = order.getMasterOrder();
        params.put("customerName", masterOrder.getUser().getFullName());
        params.put("orderNumber", order.getOrderNumber());
        params.put("oldStatus", translateStatus(oldStatus));
        params.put("newStatus", translateStatus(newStatus));
        params.put("updateDate", formatDateTime(LocalDateTime.now()));
        params.put("totalAmount", formatCurrency(orderValue));
        params.put("paymentMethod", "COD");

        // Status-specific data
        OrderStatus currentStatus = order.getOrderStatus();
        params.put("isProcessing", currentStatus == OrderStatus.PROCESSING);
        params.put("isShipped", currentStatus == OrderStatus.SHIPPED);
        params.put("isDelivered", currentStatus == OrderStatus.DELIVERED);
        params.put("isCancelled", isCancelledStatus(currentStatus));

        // Tracking info (if shipped) - these fields don't exist in current Order entity
        if (currentStatus == OrderStatus.SHIPPED) {
            params.put("trackingNumber", ""); // Order entity doesn't have trackingNumber
            params.put("carrierName", "Viettel Post"); // Default carrier
            params.put("estimatedDelivery", calculateEstimatedDelivery());
        }

        // Delivery info (if delivered) - these fields don't exist in current Order
        // entity
        if (currentStatus == OrderStatus.DELIVERED) {
            params.put("deliveryDate", formatDate(LocalDateTime.now())); // Approximate
        }

        // Cancellation info (if cancelled) - these fields don't exist in current Order
        // entity
        if (isCancelledStatus(currentStatus)) {
            params.put("cancellationDate", formatDate(LocalDateTime.now())); // Approximate
            params.put("cancellationReason", order.getInternalNotes() != null ? order.getInternalNotes() : "");
        }

        // Action URLs
        params.put("trackingUrl", buildTrackingUrl(order.getOrderNumber()));
        params.put("reviewUrl", buildReviewUrl(order.getOrderNumber()));

        // Common data
        addCommonEmailData(params);

        return params;
    }

    // === UTILITY METHODS ===

    /**
     * Helper method to check if order status is any type of cancellation
     */
    private boolean isCancelledStatus(OrderStatus status) {
        return status == OrderStatus.CANCELLED_BY_USER ||
                status == OrderStatus.CANCELLED_BY_SELLER ||
                status == OrderStatus.CANCELLED_BY_ADMIN;
    }

    /**
     * Parse specific field from address snapshot string
     * This is a simple implementation - in real system you'd use JSON parsing
     */
    private String parseAddressField(String addressSnapshot, String field) {
        if (addressSnapshot == null || addressSnapshot.isEmpty()) {
            return "";
        }

        // Simple parsing - you might want to use JSON parsing if snapshot is JSON
        try {
            String[] parts = addressSnapshot.split(",");
            return switch (field) {
                case "street" -> parts.length > 0 ? parts[0].trim() : "";
                case "ward" -> parts.length > 1 ? parts[1].trim() : "";
                case "district" -> parts.length > 2 ? parts[2].trim() : "";
                case "city" -> parts.length > 3 ? parts[3].trim() : "";
                default -> "";
            };
        } catch (Exception e) {
            log.warn("Failed to parse address field '{}' from snapshot: {}", field, addressSnapshot);
            return "";
        }
    }

    private void addCommonEmailData(Map<String, Object> params) {
        params.put("supportUrl", buildSupportUrl());
        params.put("logoUrl", buildLogoUrl());
        params.put("facebookUrl", "https://facebook.com/simplecommerce.mdm");
        params.put("instagramUrl", "https://instagram.com/simplecommerce.mdm");
        params.put("tiktokUrl", "https://tiktok.com/@simplecommerce.mdm");
        params.put("websiteUrl", "https://mdm-store.com");
        params.put("unsubscribeUrl", "https://mdm-store.com/unsubscribe");
        params.put("privacyUrl", "https://mdm-store.com/privacy");
        params.put("termsUrl", "https://mdm-store.com/terms");
        params.put("sellerTermsUrl", "https://mdm-store.com/seller-terms");
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0 ₫";
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        String s = nf.format(amount);
        // Normalize to remove currency code if present, keep symbol and spacing
        return s.replace("VND", "₫").replace("VNĐ", "₫");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String buildTrackingUrl(String orderNumber) {
        return "https://mdm-store.com/orders/" + orderNumber;
    }

    private String buildSellerDashboardUrl() {
        return "https://mdm-store.com/seller/orders";
    }

    private String buildOrderDetailsUrl(String orderNumber) {
        return "https://mdm-store.com/seller/orders/" + orderNumber;
    }

    private String buildReviewUrl(String orderNumber) {
        return "https://mdm-store.com/reviews/new?order=" + orderNumber;
    }

    private String buildSupportUrl() {
        return "https://mdm-store.com/support";
    }

    private String buildLogoUrl() {
        return "https://mdm-store.com/logo.png";
    }

    private String getProductImageUrl(com.simplecommerce_mdm.product.model.ProductVariant variant) {
        if (variant.getMainImageCloudinaryPublicId() != null) {
            return cloudinaryService.getImageUrl(variant.getMainImageCloudinaryPublicId());
        }
        return "https://via.placeholder.com/80x80.png?text=IMG";
    }

    private String getProductImageUrlSafe(OrderItem item) {
        try {
            if (item.getVariant() != null && item.getVariant().getMainImageCloudinaryPublicId() != null) {
                return cloudinaryService.getImageUrl(item.getVariant().getMainImageCloudinaryPublicId());
            }
        } catch (Exception ignored) {
        }
        return "https://via.placeholder.com/80x80.png?text=IMG";
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING_PAYMENT" -> "Chờ thanh toán";
            case "AWAITING_CONFIRMATION" -> "Chờ xác nhận";
            case "PROCESSING" -> "Đang chuẩn bị hàng";
            case "SHIPPED" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao thành công";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED_BY_USER" -> "Đã hủy bởi khách hàng";
            case "CANCELLED_BY_SELLER" -> "Đã hủy bởi người bán";
            case "CANCELLED_BY_ADMIN" -> "Đã hủy bởi admin";
            default -> status;
        };
    }

    private String calculateConfirmationDeadline(LocalDateTime orderTime) {
        return orderTime.plusHours(24).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String calculateEstimatedDelivery() {
        return LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private SendSmtpEmail createBaseEmail(String to) {
        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(new SendSmtpEmailSender().email(senderEmail).name(senderName));
        email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));
        return email;
    }
}