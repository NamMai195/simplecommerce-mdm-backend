package com.simplecommerce_mdm.email.controller;

import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.order.model.OrderItem;
import com.simplecommerce_mdm.order.repository.MasterOrderRepository;
import com.simplecommerce_mdm.order.repository.OrderRepository;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.common.enums.MasterOrderStatus;
import static com.simplecommerce_mdm.email.constants.EmailTemplateConstants.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/test-email")
@RequiredArgsConstructor
@Profile("dev") // IMPORTANT: This controller is only active in the 'dev' profile
@Tag(name = "99. Test Email", description = "Endpoints for testing email functionality (DEV only)")
public class TestEmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final MasterOrderRepository masterOrderRepository;
    private final OrderRepository orderRepository;

    @PostMapping("/simple")
    @Operation(summary = "Send a simple test email",
               description = "Sends a plain text email to the specified address. Only for development/testing.")
    public ResponseEntity<String> sendSimpleEmail(@RequestBody TestEmailRequest request) {
        emailService.sendSimpleMessage(request.to(), request.subject(), request.content());
        return ResponseEntity.ok("Simple test email sent successfully to " + request.to());
    }

    @PostMapping("/template")
    @Operation(summary = "Send a template-based test email",
               description = "Sends an email using a Brevo template. Only for development/testing.")
    public ResponseEntity<String> sendTemplateEmail(@RequestBody TestTemplateEmailRequest request) {
        emailService.sendEmailWithTemplate(request.to(), request.templateId(), request.params());
        return ResponseEntity.ok("Template test email sent successfully to " + request.to());
    }

    // === AUTH EMAIL TEST ===
    @PostMapping("/password-otp")
    @Operation(summary = "Test Password Reset OTP Email",
               description = "Sends a password reset OTP email for testing purposes")
    public ResponseEntity<String> testPasswordOtpEmail(@RequestParam String email,
                                                       @RequestParam(defaultValue = "Người dùng") String userName,
                                                       @RequestParam(defaultValue = "483920") String otpCode,
                                                       @RequestParam(defaultValue = "10") int expireMinutes) {
        emailService.sendPasswordResetOtpEmail(email, otpCode, expireMinutes, userName);
        return ResponseEntity.ok("Password OTP email sent to " + email + " (Template ID: " + PASSWORD_RESET_OTP_TEMPLATE_ID + ")");
    }

    // === ORDER EMAIL TESTS ===

    @PostMapping("/order-confirmation")
    @Operation(summary = "Test Order Confirmation Email",
               description = "Sends a test order confirmation email using sample data")
    public ResponseEntity<String> testOrderConfirmationEmail(@RequestParam String email) {
        try {
            MasterOrder sampleOrder = createSampleMasterOrder(email);
            emailService.sendOrderConfirmationEmail(sampleOrder);
            return ResponseEntity.ok("Order confirmation email sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/new-order-alert")
    @Operation(summary = "Test New Order Alert Email",
               description = "Sends a test new order alert email to seller using sample data")
    public ResponseEntity<String> testNewOrderAlertEmail(@RequestParam String sellerEmail) {
        try {
            Order sampleOrder = createSampleOrder(sellerEmail);
            emailService.sendNewOrderAlertEmail(sampleOrder);
            return ResponseEntity.ok("New order alert email sent successfully to " + sellerEmail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/order-status-update")
    @Operation(summary = "Test Order Status Update Email",
               description = "Sends a test order status update email using sample data")
    public ResponseEntity<String> testOrderStatusUpdateEmail(@RequestParam String email,
                                                              @RequestParam(defaultValue = "AWAITING_CONFIRMATION") String oldStatus,
                                                              @RequestParam(defaultValue = "PROCESSING") String newStatus) {
        try {
            Order sampleOrder = createSampleOrder(email);
            emailService.sendOrderStatusUpdateEmail(sampleOrder, oldStatus, newStatus);
            return ResponseEntity.ok("Order status update email sent successfully to " + email + 
                " (Status: " + oldStatus + " → " + newStatus + ")");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/all-order-emails")
    @Operation(summary = "Test All Order Emails",
               description = "Sends all 3 order email types to the specified email for comprehensive testing")
    public ResponseEntity<String> testAllOrderEmails(@RequestParam String email) {
        List<String> results = new ArrayList<>();
        
        try {
            // 1. Order Confirmation
            MasterOrder sampleMasterOrder = createSampleMasterOrder(email);
            emailService.sendOrderConfirmationEmail(sampleMasterOrder);
            results.add("✅ Order Confirmation sent (Template ID: " + ORDER_CONFIRMATION_TEMPLATE_ID + ")");
            
            // 2. New Order Alert (to same email for testing)
            Order sampleOrder = createSampleOrder(email);
            emailService.sendNewOrderAlertEmail(sampleOrder);
            results.add("✅ New Order Alert sent (Template ID: " + NEW_ORDER_ALERT_TEMPLATE_ID + ")");
            
            // 3. Order Status Update
            emailService.sendOrderStatusUpdateEmail(sampleOrder, "AWAITING_CONFIRMATION", "SHIPPED");
            results.add("✅ Order Status Update sent (Template ID: " + ORDER_STATUS_UPDATE_TEMPLATE_ID + ")");
            
            return ResponseEntity.ok("All order emails sent successfully to " + email + "\n\n" + String.join("\n", results));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send emails: " + e.getMessage() + 
                "\n\nPartially sent: " + String.join("\n", results));
        }
    }

    // === SAMPLE DATA CREATORS ===

    private MasterOrder createSampleMasterOrder(String customerEmail) {
        // Create sample user
        User customer = User.builder()
            .id(999L)
            .email(customerEmail)
            .firstName("Nguyễn Test")
            .lastName("Customer")
            .build();
        
        // Create sample shop
        User seller = User.builder()
            .id(998L)
            .email("seller@test.com")
            .firstName("Nguyễn Test")
            .lastName("Seller")
            .build();
        
        Shop shop = Shop.builder()
            .id(1L)
            .name("Test Shop MDM")
            .user(seller)
            .build();
        
        // Create sample master order
        MasterOrder masterOrder = MasterOrder.builder()
            .id(1L)
            .orderGroupNumber("MDM-2024-001")
            .user(customer)
            .customerEmail(customerEmail)
            .customerPhone("0123456789")
            .shippingAddressSnapshot("123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM")
            .overallStatus(MasterOrderStatus.PROCESSING)
            .totalAmountPaid(new BigDecimal("299000"))
            .paymentMethodSnapshot("COD")
            .orders(new HashSet<>())
            .build();
        masterOrder.setCreatedAt(LocalDateTime.now());
        
        // Create sample order
        Order order = createSampleOrderForMaster(masterOrder, shop);
        masterOrder.getOrders().add(order);
        
        return masterOrder;
    }
    
    private Order createSampleOrder(String customerEmail) {
        MasterOrder masterOrder = createSampleMasterOrder(customerEmail);
        return masterOrder.getOrders().iterator().next();
    }
    
    private Order createSampleOrderForMaster(MasterOrder masterOrder, Shop shop) {
        Order order = Order.builder()
            .id(1L)
            .orderNumber("MDM-2024-001-01")
            .masterOrder(masterOrder)
            .shop(shop)
            .orderStatus(OrderStatus.PROCESSING)
            .subtotalAmount(new BigDecimal("279000"))
            .shippingFee(new BigDecimal("20000"))
            .itemDiscountAmount(BigDecimal.ZERO)
            .shippingDiscountAmount(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .notesToSeller("Giao hàng giờ hành chính")
            .orderItems(new HashSet<>())
            .build();
        order.setCreatedAt(LocalDateTime.now());
        
        // Create sample order items
        OrderItem item1 = OrderItem.builder()
            .id(1L)
            .order(order)
            .productNameSnapshot("Áo Thun Nam SimpleCommerce Basic")
            .variantSkuSnapshot("TSH-BLU-M")
            .variantOptionsSnapshot("Màu: Xanh Navy, Size: M")
            .quantity(2)
            .unitPrice(new BigDecimal("129000"))
            .subtotal(new BigDecimal("258000"))
            .build();
        
        OrderItem item2 = OrderItem.builder()
            .id(2L)
            .order(order)
            .productNameSnapshot("Nón Kết MDM Store")
            .variantSkuSnapshot("CAP-BLK-OS")
            .variantOptionsSnapshot("Màu: Đen, Size: OneSize")
            .quantity(1)
            .unitPrice(new BigDecimal("79000"))
            .subtotal(new BigDecimal("79000"))
            .build();
        
        order.getOrderItems().add(item1);
        order.getOrderItems().add(item2);
        
        return order;
    }

    // --- DTOs for request bodies ---

    record TestEmailRequest(String to, String subject, String content) {}

    record TestTemplateEmailRequest(String to, Long templateId, Map<String, Object> params) {}
} 