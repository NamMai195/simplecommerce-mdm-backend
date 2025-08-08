package com.simplecommerce_mdm.order.dto;

import com.simplecommerce_mdm.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    @Size(max = 1000, message = "Internal notes must not exceed 1000 characters")
    private String internalNotes;
} 