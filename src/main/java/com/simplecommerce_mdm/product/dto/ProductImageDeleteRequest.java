package com.simplecommerce_mdm.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageDeleteRequest {
    @NotEmpty(message = "imageIds must not be empty")
    private List<Long> imageIds;
}
