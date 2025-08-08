package com.simplecommerce_mdm.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để thêm sản phẩm vào giỏ hàng")
public class AddToCartRequest {
    
    @NotNull(message = "Variant ID là bắt buộc")
    @Schema(description = "ID của product variant", example = "1")
    private Long variantId;
    
    @NotNull(message = "Số lượng là bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng sản phẩm cần thêm", example = "2")
    private Integer quantity;
} 