package com.simplecommerce_mdm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class BusinessConfigService {

    // Email Business Rules
    @Value("${business.email.confirmation-deadline-hours:24}")
    private int confirmationDeadlineHours;
    
    @Value("${business.email.delivery-estimation-days:3}")
    private int deliveryEstimationDays;

    // Social Media & Company Links
    @Value("${business.social.facebook-url:https://facebook.com/simplecommerce}")
    private String facebookUrl;
    
    @Value("${business.social.instagram-url:https://instagram.com/simplecommerce}")
    private String instagramUrl;
    
    @Value("${business.company.website-url:https://simplecommerce.com}")
    private String companyWebsiteUrl;
    
    @Value("${business.support.email:support@simplecommerce.com}")
    private String supportEmail;
    
    @Value("${business.support.phone:+84-xxx-xxx-xxx}")
    private String supportPhone;

    // Shipping & Logistics Rules
    @Value("${business.shipping.free-threshold:500000}")
    private BigDecimal shippingFreeThreshold;
    
    @Value("${business.shipping.default-fee:30000}")
    private BigDecimal shippingDefaultFee;
    
    @Value("${business.shipping.default-carrier:Viettel Post}")
    private String shippingDefaultCarrier;

    // Commission & Revenue Rules
    @Value("${business.commission.admin-rate:0.05}")
    private BigDecimal adminCommissionRate;
    
    @Value("${business.commission.payment-gateway-rate:0.025}")
    private BigDecimal paymentGatewayCommissionRate;

    // Order & Inventory Rules
    @Value("${business.order.stock-update-max-retries:3}")
    private int stockUpdateMaxRetries;

    @EventListener(ApplicationReadyEvent.class)
    public void logBusinessConfiguration() {
        log.info("🚀 ================================================================================================");
        log.info("📊 BUSINESS CONFIGURATION LOADED - SimpleCommerce MDM Backend");
        log.info("🚀 ================================================================================================");
        
        log.info("📧 EMAIL BUSINESS RULES:");
        log.info("   ⏰ Confirmation Deadline: {} hours", confirmationDeadlineHours);
        log.info("   🚚 Delivery Estimation: {} days", deliveryEstimationDays);
        
        log.info("🌐 SOCIAL MEDIA & COMPANY LINKS:");
        log.info("   📘 Facebook: {}", facebookUrl);
        log.info("   📷 Instagram: {}", instagramUrl);
        log.info("   🌐 Website: {}", companyWebsiteUrl);
        log.info("   📧 Support Email: {}", supportEmail);
        log.info("   📞 Support Phone: {}", supportPhone);
        
        log.info("🚚 SHIPPING & LOGISTICS RULES:");
        log.info("   💰 Free Shipping Threshold: {} VND", shippingFreeThreshold);
        log.info("   💸 Default Shipping Fee: {} VND", shippingDefaultFee);
        log.info("   📦 Default Carrier: {}", shippingDefaultCarrier);
        
        log.info("💰 COMMISSION & REVENUE RULES:");
        log.info("   🏛️ Admin Commission Rate: {}% ({} decimal)", 
                adminCommissionRate.multiply(BigDecimal.valueOf(100)), adminCommissionRate);
        log.info("   💳 Payment Gateway Rate: {}% ({} decimal)", 
                paymentGatewayCommissionRate.multiply(BigDecimal.valueOf(100)), paymentGatewayCommissionRate);
        
        log.info("📋 ORDER & INVENTORY RULES:");
        log.info("   🔄 Stock Update Max Retries: {}", stockUpdateMaxRetries);
        
        log.info("🚀 ================================================================================================");
        log.info("✅ All Business Configuration Variables Successfully Loaded!");
        log.info("🚀 ================================================================================================");
    }

    // Getter methods để các services khác có thể access
    public int getConfirmationDeadlineHours() { return confirmationDeadlineHours; }
    public int getDeliveryEstimationDays() { return deliveryEstimationDays; }
    public String getFacebookUrl() { return facebookUrl; }
    public String getInstagramUrl() { return instagramUrl; }
    public String getCompanyWebsiteUrl() { return companyWebsiteUrl; }
    public String getSupportEmail() { return supportEmail; }
    public String getSupportPhone() { return supportPhone; }
    public BigDecimal getShippingFreeThreshold() { return shippingFreeThreshold; }
    public BigDecimal getShippingDefaultFee() { return shippingDefaultFee; }
    public String getShippingDefaultCarrier() { return shippingDefaultCarrier; }
    public BigDecimal getAdminCommissionRate() { return adminCommissionRate; }
    public BigDecimal getPaymentGatewayCommissionRate() { return paymentGatewayCommissionRate; }
    public int getStockUpdateMaxRetries() { return stockUpdateMaxRetries; }

    /**
     * Method để log business configuration usage khi được gọi
     */
    public void logConfigurationUsage(String serviceName, String configType, Object value) {
        log.info("🔧 [{}] Using business config: {} = {}", serviceName, configType, value);
    }

    /**
     * Method để validate business configuration values
     */
    public boolean validateBusinessConfig() {
        boolean isValid = true;
        
        if (shippingFreeThreshold.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("❌ Invalid shipping free threshold: {}", shippingFreeThreshold);
            isValid = false;
        }
        
        if (shippingDefaultFee.compareTo(BigDecimal.ZERO) < 0) {
            log.error("❌ Invalid shipping default fee: {}", shippingDefaultFee);
            isValid = false;
        }
        
        if (adminCommissionRate.compareTo(BigDecimal.ZERO) < 0 || 
            adminCommissionRate.compareTo(BigDecimal.ONE) > 0) {
            log.error("❌ Invalid admin commission rate: {}", adminCommissionRate);
            isValid = false;
        }
        
        if (confirmationDeadlineHours <= 0) {
            log.error("❌ Invalid confirmation deadline: {} hours", confirmationDeadlineHours);
            isValid = false;
        }
        
        if (isValid) {
            log.info("✅ All business configuration values are valid");
        }
        
        return isValid;
    }
}
