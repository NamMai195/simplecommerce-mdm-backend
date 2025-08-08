package com.simplecommerce_mdm.cart.service.impl;

import com.simplecommerce_mdm.cart.dto.*;
import com.simplecommerce_mdm.cart.model.Cart;
import com.simplecommerce_mdm.cart.model.CartItem;
import com.simplecommerce_mdm.cart.repository.CartItemRepository;
import com.simplecommerce_mdm.cart.repository.CartRepository;
import com.simplecommerce_mdm.cart.service.CartService;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.product.model.ProductVariant;
import com.simplecommerce_mdm.product.repository.ProductVariantRepository;
import com.simplecommerce_mdm.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    // Cart expiration time: 30 days
    private static final int CART_EXPIRATION_DAYS = 30;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        log.info("Getting cart for user: {}", user.getEmail());

        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        
        if (cartOptional.isEmpty()) {
            // Return empty cart
            return CartResponse.builder()
                    .id(null)
                    .userId(user.getId())
                    .items(List.of())
                    .totalItems(0)
                    .totalQuantity(0)
                    .subtotal(BigDecimal.ZERO)
                    .isEmpty(true)
                    .hasPriceChanges(false)
                    .hasOutOfStockItems(false)
                    .build();
        }

        Cart cart = cartOptional.get();
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(CustomUserDetails userDetails, AddToCartRequest request) {
        User user = userDetails.getUser();
        log.info("Adding to cart for user: {}, variantId: {}, quantity: {}", 
                user.getEmail(), request.getVariantId(), request.getQuantity());

        // Validate variant exists and is active
        ProductVariant variant = productVariantRepository.findActiveVariantById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant not found or not available: " + request.getVariantId()));

        // Check stock availability
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new InvalidDataException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                            variant.getStockQuantity(), request.getQuantity()));
        }

        // Get or create cart
        Cart cart = getOrCreateCart(user);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndVariant(cart, variant);

        if (existingItem.isPresent()) {
            // Update existing item
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check total stock for new quantity
            if (variant.getStockQuantity() < newQuantity) {
                throw new InvalidDataException(
                        String.format("Insufficient stock. Available: %d, In cart: %d, Requesting additional: %d", 
                                variant.getStockQuantity(), cartItem.getQuantity(), request.getQuantity()));
            }
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            log.info("Updated existing cart item. New quantity: {}", newQuantity);
        } else {
            // Create new cart item
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .priceAtAddition(variant.getFinalPrice())
                    .addedAt(OffsetDateTime.now())
                    .build();
            
            cartItemRepository.save(newCartItem);
            log.info("Created new cart item for variant: {}", variant.getId());
        }

        // Update cart expiration
        cart.setExpiresAt(OffsetDateTime.now().plusDays(CART_EXPIRATION_DAYS));
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(CustomUserDetails userDetails, Long itemId, UpdateCartItemRequest request) {
        User user = userDetails.getUser();
        log.info("Updating cart item {} for user: {}, new quantity: {}", 
                itemId, user.getEmail(), request.getQuantity());

        // Find cart item with security check
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found or you don't have permission to access it: " + itemId));

        // Check stock availability
        ProductVariant variant = cartItem.getVariant();
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new InvalidDataException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                            variant.getStockQuantity(), request.getQuantity()));
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // Update cart expiration
        Cart cart = cartItem.getCart();
        cart.setExpiresAt(OffsetDateTime.now().plusDays(CART_EXPIRATION_DAYS));
        cartRepository.save(cart);

        log.info("Updated cart item {} to quantity: {}", itemId, request.getQuantity());
        
        return buildCartItemResponse(cartItem);
    }

    @Override
    @Transactional
    public void removeCartItem(CustomUserDetails userDetails, Long itemId) {
        User user = userDetails.getUser();
        log.info("Removing cart item {} for user: {}", itemId, user.getEmail());

        // Find cart item with security check
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found or you don't have permission to access it: " + itemId));

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item: {}", itemId);
    }

    @Override
    @Transactional
    public void clearCart(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        log.info("Clearing cart for user: {}", user.getEmail());

        cartItemRepository.deleteByCartUserId(user.getId());
        log.info("Cleared all cart items for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCartItemCount(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return cartRepository.countItemsByUserId(user.getId());
    }

    // ===== PRIVATE HELPER METHODS =====

    private Cart getOrCreateCart(User user) {
        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        } else {
            // Create new cart
            Cart newCart = Cart.builder()
                    .user(user)
                    .expiresAt(OffsetDateTime.now().plusDays(CART_EXPIRATION_DAYS))
                    .build();
            
            return cartRepository.save(newCart);
        }
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCartUserId(cart.getUser().getId());
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::buildCartItemResponse)
                .collect(Collectors.toList());

        // Calculate totals
        int totalItems = itemResponses.size();
        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
        
        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check for price changes and out of stock items
        boolean hasPriceChanges = itemResponses.stream()
                .anyMatch(CartItemResponse::getPriceChanged);
        
        boolean hasOutOfStockItems = itemResponses.stream()
                .anyMatch(item -> !item.getInStock());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .expiresAt(cart.getExpiresAt())
                .updatedAt(cart.getUpdatedAt())
                .hasPriceChanges(hasPriceChanges)
                .hasOutOfStockItems(hasOutOfStockItems)
                .isEmpty(totalItems == 0)
                .build();
    }

    private CartItemResponse buildCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getVariant();
        
        // Get current variant data
        ProductVariant currentVariant = productVariantRepository.findById(variant.getId())
                .orElse(variant); // Fallback to cached variant if not found

        // Calculate price change
        boolean priceChanged = !cartItem.getPriceAtAddition().equals(currentVariant.getFinalPrice());
        
        // Check stock
        boolean inStock = currentVariant.getStockQuantity() >= cartItem.getQuantity();
        
        // Calculate subtotal
        BigDecimal subtotal = cartItem.getPriceAtAddition()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        // Get variant image URL
        String imageUrl = null;
        if (currentVariant.getMainImageCloudinaryPublicId() != null) {
            imageUrl = cloudinaryService.getImageUrl(currentVariant.getMainImageCloudinaryPublicId());
        }

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .variantId(variant.getId())
                .variantSku(variant.getSku())
                .variantOptions(variant.getOptions())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .productSlug(variant.getProduct().getSlug())
                .shopName(variant.getProduct().getShop().getName())
                .variantImageUrl(imageUrl)
                .quantity(cartItem.getQuantity())
                .priceAtAddition(cartItem.getPriceAtAddition())
                .currentPrice(currentVariant.getFinalPrice())
                .subtotal(subtotal)
                .priceChanged(priceChanged)
                .inStock(inStock)
                .stockQuantity(currentVariant.getStockQuantity())
                .addedAt(cartItem.getAddedAt())
                .build();
    }
} 