package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private OffsetDateTime createdAt;
    private List<ProductVariantResponse> variants;
    private List<String> imageUrls; // Or a more complex image object

    @Data
    public static class ProductVariantResponse {
        private String sku;
        private BigDecimal finalPrice;
        private Integer stockQuantity;
        private String options;
    }
} 