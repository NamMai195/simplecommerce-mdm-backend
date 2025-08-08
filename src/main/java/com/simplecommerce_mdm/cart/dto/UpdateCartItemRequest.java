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
@Schema(description = "Request để cập nhật số lượng item trong giỏ hàng")
public class UpdateCartItemRequest {
    
    @NotNull(message = "Số lượng là bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng mới", example = "3")
    private Integer quantity;
} 