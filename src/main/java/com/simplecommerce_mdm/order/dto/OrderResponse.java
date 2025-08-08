package com.simplecommerce_mdm.order.dto;

import com.simplecommerce_mdm.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long masterOrderId;
    private String orderGroupNumber;
    
    // Shop info
    private Long shopId;
    private String shopName;
    
    // Order status
    private OrderStatus orderStatus;
    
    // Amounts
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal itemDiscountAmount;
    private BigDecimal shippingDiscountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    // Shipping info
    private String shippingMethodNameSnapshot;
    
    // Notes
    private String notesToSeller;
    
    // Timestamps
    private OffsetDateTime orderedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Order items
    private List<OrderItemResponse> orderItems;
    
    // Count and summary
    private Integer totalItems;
    private Integer totalQuantity;
} 