package com.simplecommerce_mdm.config.controller;

import com.simplecommerce_mdm.config.BusinessConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/business-config")
@RequiredArgsConstructor
@Tag(name = "Business Configuration", description = "Admin endpoints for viewing business configuration")
@SecurityRequirement(name = "bearerAuth")
public class BusinessConfigController {

    private final BusinessConfigService businessConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get current business configuration", 
               description = "Returns all loaded business configuration values")
    public Map<String, Object> getBusinessConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        // Email Business Rules
        Map<String, Object> emailConfig = new HashMap<>();
        emailConfig.put("confirmationDeadlineHours", businessConfigService.getConfirmationDeadlineHours());
        emailConfig.put("deliveryEstimationDays", businessConfigService.getDeliveryEstimationDays());
        config.put("email", emailConfig);
        
        // Social Media & Company Links
        Map<String, Object> socialConfig = new HashMap<>();
        socialConfig.put("facebookUrl", businessConfigService.getFacebookUrl());
        socialConfig.put("instagramUrl", businessConfigService.getInstagramUrl());
        socialConfig.put("companyWebsiteUrl", businessConfigService.getCompanyWebsiteUrl());
        socialConfig.put("supportEmail", businessConfigService.getSupportEmail());
        socialConfig.put("supportPhone", businessConfigService.getSupportPhone());
        config.put("social", socialConfig);
        
        // Shipping & Logistics Rules
        Map<String, Object> shippingConfig = new HashMap<>();
        shippingConfig.put("freeThreshold", businessConfigService.getShippingFreeThreshold());
        shippingConfig.put("defaultFee", businessConfigService.getShippingDefaultFee());
        shippingConfig.put("defaultCarrier", businessConfigService.getShippingDefaultCarrier());
        config.put("shipping", shippingConfig);
        
        // Commission & Revenue Rules
        Map<String, Object> commissionConfig = new HashMap<>();
        commissionConfig.put("adminRate", businessConfigService.getAdminCommissionRate());
        commissionConfig.put("adminRatePercentage", businessConfigService.getAdminCommissionRate().multiply(BigDecimal.valueOf(100)));
        commissionConfig.put("paymentGatewayRate", businessConfigService.getPaymentGatewayCommissionRate());
        commissionConfig.put("paymentGatewayRatePercentage", businessConfigService.getPaymentGatewayCommissionRate().multiply(BigDecimal.valueOf(100)));
        config.put("commission", commissionConfig);
        
        // Order & Inventory Rules
        Map<String, Object> orderConfig = new HashMap<>();
        orderConfig.put("stockUpdateMaxRetries", businessConfigService.getStockUpdateMaxRetries());
        config.put("order", orderConfig);
        
        // Validation status
        config.put("isValid", businessConfigService.validateBusinessConfig());
        
        return config;
    }

    @GetMapping("/validation")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate business configuration", 
               description = "Validates all business configuration values and returns status")
    public Map<String, Object> validateConfiguration() {
        Map<String, Object> result = new HashMap<>();
        boolean isValid = businessConfigService.validateBusinessConfig();
        result.put("isValid", isValid);
        result.put("message", isValid ? "All business configuration values are valid" : "Some business configuration values are invalid");
        return result;
    }
}
