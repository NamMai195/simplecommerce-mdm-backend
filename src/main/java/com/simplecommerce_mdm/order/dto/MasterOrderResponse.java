package com.simplecommerce_mdm.order.dto;

import com.simplecommerce_mdm.common.enums.MasterOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterOrderResponse {

    private Long id;
    private String orderGroupNumber;
    
    // Customer info
    private Long userId;
    private String customerEmail;
    private String customerPhone;
    
    // Address info
    private String shippingAddressSnapshot;
    private String billingAddressSnapshot;
    
    // Overall status
    private MasterOrderStatus overallStatus;
    
    // Payment info
    private BigDecimal totalAmountPaid;
    private BigDecimal totalDiscountAmount;
    private String paymentMethodSnapshot;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Sub-orders
    private List<OrderResponse> orders;
    
    // Summary
    private Integer totalOrders;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal grandTotal;
} 