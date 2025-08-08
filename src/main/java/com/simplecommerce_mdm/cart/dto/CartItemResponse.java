package com.simplecommerce_mdm.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin item trong giỏ hàng")
public class CartItemResponse {
    
    @Schema(description = "ID của cart item", example = "1")
    private Long id;
    
    @Schema(description = "ID của product variant", example = "1")
    private Long variantId;
    
    @Schema(description = "SKU của variant", example = "PHONE-128GB-RED")
    private String variantSku;
    
    @Schema(description = "Tùy chọn variant (JSON)", example = "{\"Storage\":\"128GB\",\"Color\":\"Red\"}")
    private String variantOptions;
    
    @Schema(description = "ID sản phẩm", example = "1")
    private Long productId;
    
    @Schema(description = "Tên sản phẩm", example = "iPhone 15")
    private String productName;
    
    @Schema(description = "Slug sản phẩm", example = "iphone-15")
    private String productSlug;
    
    @Schema(description = "Tên shop", example = "Apple Store")
    private String shopName;
    
    @Schema(description = "URL ảnh chính của variant")
    private String variantImageUrl;
    
    @Schema(description = "Số lượng", example = "2")
    private Integer quantity;
    
    @Schema(description = "Giá tại thời điểm thêm vào giỏ", example = "999.99")
    private BigDecimal priceAtAddition;
    
    @Schema(description = "Giá hiện tại của variant", example = "899.99")
    private BigDecimal currentPrice;
    
    @Schema(description = "Tổng tiền của item này", example = "1999.98")
    private BigDecimal subtotal;
    
    @Schema(description = "Có thay đổi giá so với lúc thêm vào không", example = "true")
    private Boolean priceChanged;
    
    @Schema(description = "Còn hàng trong kho không", example = "true")
    private Boolean inStock;
    
    @Schema(description = "Số lượng tồn kho", example = "50")
    private Integer stockQuantity;
    
    @Schema(description = "Thời gian thêm vào giỏ")
    private OffsetDateTime addedAt;
} 