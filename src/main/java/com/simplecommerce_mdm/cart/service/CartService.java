package com.simplecommerce_mdm.cart.service;

import com.simplecommerce_mdm.cart.dto.*;
import com.simplecommerce_mdm.config.CustomUserDetails;

public interface CartService {
    
    /**
     * Lấy giỏ hàng của user hiện tại
     */
    CartResponse getCart(CustomUserDetails userDetails);
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    CartResponse addToCart(CustomUserDetails userDetails, AddToCartRequest request);
    
    /**
     * Cập nhật số lượng item trong giỏ hàng
     */
    CartItemResponse updateCartItem(CustomUserDetails userDetails, Long itemId, UpdateCartItemRequest request);
    
    /**
     * Xóa item khỏi giỏ hàng
     */
    void removeCartItem(CustomUserDetails userDetails, Long itemId);
    
    /**
     * Xóa tất cả items trong giỏ hàng
     */
    void clearCart(CustomUserDetails userDetails);
    
    /**
     * Đếm số lượng items trong giỏ hàng
     */
    Long getCartItemCount(CustomUserDetails userDetails);
} 