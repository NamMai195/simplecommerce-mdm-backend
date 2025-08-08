package com.simplecommerce_mdm.cart.controller;

import com.simplecommerce_mdm.cart.dto.*;
import com.simplecommerce_mdm.cart.service.CartService;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs quản lý giỏ hàng cho USER đã đăng nhập")
@PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Lấy giỏ hàng hiện tại",
            description = "Lấy thông tin chi tiết giỏ hàng của user đang đăng nhập bao gồm tất cả items, tổng tiền và thông tin khác"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy giỏ hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CartResponse cart = cartService.getCart(userDetails);
        
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Lấy giỏ hàng thành công")
                        .data(cart)
                        .build()
        );
    }

    @PostMapping("/items")
    @Operation(
            summary = "Thêm sản phẩm vào giỏ hàng",
            description = "Thêm một variant của sản phẩm vào giỏ hàng. Nếu sản phẩm đã có trong giỏ thì tăng số lượng."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thêm vào giỏ hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product variant không tồn tại")
    })
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        
        log.info("Adding to cart - User: {}, VariantId: {}, Quantity: {}", 
                userDetails.getUser().getEmail(), request.getVariantId(), request.getQuantity());
        
        CartResponse cart = cartService.addToCart(userDetails, request);
        
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Thêm sản phẩm vào giỏ hàng thành công")
                        .data(cart)
                        .build()
        );
    }

    @PutMapping("/items/{itemId}")
    @Operation(
            summary = "Cập nhật số lượng item trong giỏ hàng",
            description = "Cập nhật số lượng của một item cụ thể trong giỏ hàng"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item không tồn tại hoặc không có quyền truy cập")
    })
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID của cart item cần cập nhật") @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        log.info("Updating cart item - User: {}, ItemId: {}, New Quantity: {}", 
                userDetails.getUser().getEmail(), itemId, request.getQuantity());
        
        CartItemResponse updatedItem = cartService.updateCartItem(userDetails, itemId, request);
        
        return ResponseEntity.ok(
                ApiResponse.<CartItemResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Cập nhật item giỏ hàng thành công")
                        .data(updatedItem)
                        .build()
        );
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(
            summary = "Xóa item khỏi giỏ hàng",
            description = "Xóa một item cụ thể khỏi giỏ hàng"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item không tồn tại hoặc không có quyền truy cập")
    })
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID của cart item cần xóa") @PathVariable Long itemId) {
        
        log.info("Removing cart item - User: {}, ItemId: {}", 
                userDetails.getUser().getEmail(), itemId);
        
        cartService.removeCartItem(userDetails, itemId);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Xóa item khỏi giỏ hàng thành công")
                        .build()
        );
    }

    @DeleteMapping
    @Operation(
            summary = "Xóa tất cả items trong giỏ hàng",
            description = "Xóa toàn bộ items trong giỏ hàng của user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa toàn bộ giỏ hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Clearing cart - User: {}", userDetails.getUser().getEmail());
        
        cartService.clearCart(userDetails);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Xóa toàn bộ giỏ hàng thành công")
                        .build()
        );
    }

    @GetMapping("/count")
    @Operation(
            summary = "Đếm số lượng items trong giỏ hàng",
            description = "Lấy tổng số items (không phải quantity) trong giỏ hàng của user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy số lượng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<ApiResponse<Long>> getCartItemCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long count = cartService.getCartItemCount(userDetails);
        
        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Lấy số lượng items thành công")
                        .data(count)
                        .build()
        );
    }
} 