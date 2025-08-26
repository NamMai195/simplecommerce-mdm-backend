package com.simplecommerce_mdm.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageItem {
    private Long id;
    private String url;
    private String publicId;
    private Boolean isPrimary;
    private Integer sortOrder;
}
