package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private Integer categoryId;
    private Long shopId;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private List<ProductVariantResponse> variants;
    private List<String> imageUrls; // Or a more complex image object

    @Data
    public static class ProductVariantResponse {
        private Long id;
        private String sku;
        private BigDecimal finalPrice;
        private BigDecimal compareAtPrice;
        private Integer stockQuantity;
        private String options;
        private Boolean isActive;
    }
} 