package com.simplecommerce_mdm.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "Product name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "Base price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Category ID is mandatory")
    private Integer categoryId;

    @NotEmpty(message = "Product must have at least one variant")
    @Valid // This ensures that the validation rules on ProductVariantRequest are triggered
    private List<ProductVariantRequest> variants;
} 