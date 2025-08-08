package com.simplecommerce_mdm.order.dto;

import com.simplecommerce_mdm.common.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long variantId;
    private String productNameSnapshot;
    private String variantSkuSnapshot;
    private String variantOptionsSnapshot;
    private String variantImageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private OrderItemStatus status;
} 