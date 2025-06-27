package com.simplecommerce_mdm.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    @NotBlank(message = "SKU is mandatory")
    private String sku;

    @NotNull(message = "Final price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal finalPrice;

    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is mandatory")
    private Integer stockQuantity;
    
    // JSON string for options like {"color": "Red", "size": "XL"}
    private String options; 
} 