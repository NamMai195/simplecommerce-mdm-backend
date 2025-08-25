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
public class OrderListResponse {

    private Long id;
    private String orderNumber;
    private String orderGroupNumber;
    
    // Shop info
    private Long shopId;
    private String shopName;
    
    // Customer info (for admin/seller view)
    private String customerEmail;
    
    // Shipping address info (for seller to see delivery location)
    private String shippingAddress;
    private String contactName;
    private String contactPhone;
    
    // Order status
    private OrderStatus orderStatus;
    
    // Summary amounts
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    
    // Order summary
    private Integer totalItems;
    private Integer totalQuantity;
    
    // Product images for frontend display
    private List<String> productImageUrls;
    
    // Timestamps
    private OffsetDateTime orderedAt;
    private LocalDateTime createdAt;
} 