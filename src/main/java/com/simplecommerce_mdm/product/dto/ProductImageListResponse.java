package com.simplecommerce_mdm.product.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductImageListResponse {
    private Long productId;
    private int count;
    private java.util.List<ProductImageItem> images;
}
