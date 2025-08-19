package com.simplecommerce_mdm.shop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopProfileResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;        // avatar
    private String coverImageUrl;  // background
    private BigDecimal rating;
    private Integer totalProducts;
}


