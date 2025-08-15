package com.simplecommerce_mdm.order.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CheckoutRequest {

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private Long billingAddressId; // Optional - if null, will use shipping address

    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    private String customerPhone;

    @Size(max = 500, message = "Notes to seller must not exceed 500 characters")
    private String notesToSeller;

    @NotNull(message = "Payment method code is required")
    @NotBlank(message = "Payment method code cannot be blank")
    private String paymentMethodCode;

    // Optional: specific shipping method (can be null for default)
    private Long shippingMethodId;
} 