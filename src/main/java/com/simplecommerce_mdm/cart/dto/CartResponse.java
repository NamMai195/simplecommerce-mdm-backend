package com.simplecommerce_mdm.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin toàn bộ giỏ hàng")
public class CartResponse {
    
    @Schema(description = "ID của cart")
    private UUID id;
    
    @Schema(description = "ID của user")
    private Long userId;
    
    @Schema(description = "Danh sách items trong giỏ hàng")
    private List<CartItemResponse> items;
    
    @Schema(description = "Tổng số items trong giỏ", example = "3")
    private Integer totalItems;
    
    @Schema(description = "Tổng số lượng sản phẩm", example = "5")
    private Integer totalQuantity;
    
    @Schema(description = "Tổng tiền tạm tính (chưa ship + tax)", example = "2999.97")
    private BigDecimal subtotal;
    
    @Schema(description = "Thời gian hết hạn giỏ hàng")
    private OffsetDateTime expiresAt;
    
    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Có item nào thay đổi giá không", example = "false")
    private Boolean hasPriceChanges;
    
    @Schema(description = "Có item nào hết hàng không", example = "false")
    private Boolean hasOutOfStockItems;
    
    @Schema(description = "Giỏ hàng có trống không", example = "false")
    private Boolean isEmpty;
} 