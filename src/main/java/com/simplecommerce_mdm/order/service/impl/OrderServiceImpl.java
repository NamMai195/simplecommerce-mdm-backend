package com.simplecommerce_mdm.order.service.impl;

import com.simplecommerce_mdm.order.service.OrderService;
import com.simplecommerce_mdm.order.dto.*;
import com.simplecommerce_mdm.order.model.*;
import com.simplecommerce_mdm.order.repository.*;
import com.simplecommerce_mdm.cart.model.Cart;
import com.simplecommerce_mdm.cart.model.CartItem;
import com.simplecommerce_mdm.cart.repository.CartRepository;
import com.simplecommerce_mdm.cart.repository.CartItemRepository;
import com.simplecommerce_mdm.product.model.ProductVariant;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ProductVariantRepository;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.product.repository.ProductImageRepository;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.model.Address;
import com.simplecommerce_mdm.user.model.UserAddress;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.user.repository.AddressRepository;
import com.simplecommerce_mdm.user.repository.UserAddressRepository;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.email.events.OrderEmailEvents;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.common.enums.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final MasterOrderRepository masterOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ShopRepository shopRepository;
    private final ProductImageRepository productImageRepository;
    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MasterOrderResponse createOrderFromCart(Long userId, CheckoutRequest request) {
        log.info("Creating order from cart for user: {}", userId);
        
        // 1. Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 2. Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidDataException("Cart is empty"));
        
        List<CartItem> cartItems = cartItemRepository.findByCartUserId(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidDataException("Cart is empty");
        }
        
        // 3. Validate payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findByCode(request.getPaymentMethodCode())
                .orElseThrow(() -> new InvalidDataException("Invalid payment method"));
        
        if (!paymentMethod.getIsActive()) {
            throw new InvalidDataException("Payment method is not active");
        }
        
        // 4. Validate stock for all items
        validateStockForCartItems(cartItems);
        
        // 5. Group cart items by shop
        Map<Shop, List<CartItem>> itemsByShop = groupCartItemsByShop(cartItems);
        
        // 6. Generate order group number
        String orderGroupNumber = generateOrderGroupNumber();
        
        // 7. Create master order
        MasterOrder masterOrder = createMasterOrder(user, request, paymentMethod, orderGroupNumber);
        masterOrder = masterOrderRepository.save(masterOrder);
        
        // 8. Create individual orders for each shop
        List<Order> orders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Map.Entry<Shop, List<CartItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();
            
            Order order = createOrderForShop(masterOrder, shop, shopItems, request);
            order = orderRepository.save(order);
            orders.add(order);
            
            // Create order items
            createOrderItems(order, shopItems);
            
            totalAmount = totalAmount.add(order.getSubtotalAmount().add(order.getShippingFee()));
        }
        
        // 9. Update master order total
        masterOrder.setTotalAmountPaid(totalAmount);
        masterOrderRepository.save(masterOrder);
        
        // 10. Create COD payment record
        createCODPayment(masterOrder, paymentMethod, totalAmount);
        
        // 11. Update inventory
        updateInventoryForOrder(cartItems);
        
        // 12. Clear user's cart
        clearUserCart(userId);
        
        // 13. Publish email events (async AFTER_COMMIT)
        publishOrderEmails(masterOrder);
        
        log.info("Order created successfully. Order group: {}", orderGroupNumber);
        
        return buildMasterOrderResponse(masterOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Validate user ownership
        if (!order.getMasterOrder().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        
        return buildOrderResponse(order);
    }

    // Admin: get order details by orderId
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return buildOrderResponse(order);
    }

    // Seller: get order details, validating ownership by shopId
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsForSeller(Long shopId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getShop().getId().equals(shopId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        return buildOrderResponse(order);
    }

    // Admin: list master orders
    @Override
    @Transactional(readOnly = true)
    public Page<MasterOrderResponse> getAllMasterOrdersForAdmin(Pageable pageable) {
        Page<MasterOrder> mos = masterOrderRepository.findAll(pageable);
        return mos.map(this::buildMasterOrderResponse);
    }

    // Admin: get master order details
    @Override
    @Transactional(readOnly = true)
    public MasterOrderResponse getMasterOrderDetailsForAdmin(Long masterOrderId) {
        MasterOrder mo = masterOrderRepository.findById(masterOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Master order not found"));
        return buildMasterOrderResponse(mo);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterOrderResponse getMasterOrderDetails(Long userId, Long masterOrderId) {
        MasterOrder masterOrder = masterOrderRepository.findById(masterOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Validate user ownership
        if (!masterOrder.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        
        return buildMasterOrderResponse(masterOrder);
    }

    @Override
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Validate user ownership
        if (!order.getMasterOrder().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        
        // Check if order can be cancelled
        if (!canCancelOrder(order.getOrderStatus())) {
            throw new InvalidDataException("Order cannot be cancelled in current status");
        }
        
        // Update order status
        order.setOrderStatus(OrderStatus.CANCELLED_BY_USER);
        order.setInternalNotes(reason);
        orderRepository.save(order);
        
        // Restore inventory
        restoreInventoryForOrder(order);
        
        // Recalculate master order overall status after a child order is cancelled
        updateMasterOrderStatus(order.getMasterOrder());
        
        log.info("Order {} cancelled by user {}", orderId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getSellerOrders(Long shopId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getSellerOrdersByStatus(Long shopId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByShopIdAndOrderStatusOrderByCreatedAtDesc(shopId, status, pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        OrderStatus oldStatus = order.getOrderStatus();
        OrderStatus newStatus = request.getOrderStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new InvalidDataException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }
        
        // Update order
        order.setOrderStatus(newStatus);
        if (request.getInternalNotes() != null) {
            order.setInternalNotes(request.getInternalNotes());
        }
        
        // Note: Current Order entity doesn't have specific timestamp fields for status changes
        // You may want to add these fields later: processingAt, shippedAt, deliveredAt, cancelledAt
        
        order = orderRepository.save(order);
        
        // Update master order status if needed
        updateMasterOrderStatus(order.getMasterOrder());
        
        // Publish status update email event
        publishOrderStatusUpdateEmail(order, oldStatus.toString(), newStatus.toString());
        
        log.info("Order {} status updated from {} to {}", orderId, oldStatus, newStatus);
        
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getAllOrdersForAdmin(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> searchOrdersForAdmin(String keyword, Pageable pageable) {
        Page<Order> orders = orderRepository.searchOrders(keyword, pageable);
        return orders.map(this::buildOrderListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsResponse getOrderStatistics() {
        return OrderStatisticsResponse.builder()
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByOrderStatus(OrderStatus.PENDING_PAYMENT))
                .awaitingConfirmationOrders(orderRepository.countByOrderStatus(OrderStatus.AWAITING_CONFIRMATION))
                .processingOrders(orderRepository.countByOrderStatus(OrderStatus.PROCESSING))
                .shippedOrders(orderRepository.countByOrderStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByOrderStatus(OrderStatus.DELIVERED))
                .completedOrders(orderRepository.countByOrderStatus(OrderStatus.COMPLETED))
                .cancelledOrders(orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_USER) +
                                orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_SELLER) +
                                orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_ADMIN))
                .build();
    }

    // Helper methods
    
    private void validateStockForCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findActiveVariantById(item.getVariant().getId())
                    .orElseThrow(() -> new InvalidDataException("Product variant not available"));
            
            if (!productVariantRepository.hasEnoughStock(variant.getId(), item.getQuantity())) {
                throw new InvalidDataException("Insufficient stock for " + variant.getSku());
            }
        }
    }
    
    private Map<Shop, List<CartItem>> groupCartItemsByShop(List<CartItem> cartItems) {
        return cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getVariant().getProduct().getShop()));
    }
    
    private String generateOrderGroupNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 1000));
        return "MO" + timestamp + random;
    }
    
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 1000));
        return "ORD" + timestamp + random;
    }
    
    private MasterOrder createMasterOrder(User user, CheckoutRequest request, PaymentMethod paymentMethod, String orderGroupNumber) {
        // Fetch shipping UserAddress (to get both address and contact info)
        UserAddress shippingUserAddress = userAddressRepository.findByIdAndUserId(request.getShippingAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + request.getShippingAddressId()));
        Address shippingAddress = shippingUserAddress.getAddress();
        
        // Fetch billing address (use shipping address if not specified)
        Address billingAddress = shippingAddress;
        UserAddress billingUserAddress = shippingUserAddress;
        if (request.getBillingAddressId() != null) {
            billingUserAddress = userAddressRepository.findByIdAndUserId(request.getBillingAddressId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Billing address not found with id: " + request.getBillingAddressId()));
            billingAddress = billingUserAddress.getAddress();
        }
        
        // Create address snapshots
        String shippingAddressSnapshot = formatAddressSnapshot(shippingAddress);
        String billingAddressSnapshot = formatAddressSnapshot(billingAddress);
        
        return MasterOrder.builder()
                .orderGroupNumber(orderGroupNumber)
                .user(user)
                .customerEmail(user.getEmail())
                .customerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : user.getPhoneNumber())
                .shippingAddressSnapshot(shippingAddressSnapshot)
                .billingAddressSnapshot(billingAddressSnapshot)
                .shippingContactName(shippingUserAddress.getContactFullName())
                .shippingContactPhone(shippingUserAddress.getContactPhoneNumber())
                .overallStatus(MasterOrderStatus.AWAITING_CONFIRMATION) // COD starts with awaiting confirmation
                .totalAmountPaid(BigDecimal.ZERO) // Will be updated later
                .paymentMethodSnapshot(paymentMethod.getName())
                .build();
    }
    
    private Order createOrderForShop(MasterOrder masterOrder, Shop shop, List<CartItem> shopItems, CheckoutRequest request) {
        BigDecimal subtotal = shopItems.stream()
                .map(item -> item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shippingFee = calculateShippingFee(shop, subtotal); // Simple calculation
        
        return Order.builder()
                .masterOrder(masterOrder)
                .orderNumber(generateOrderNumber())
                .shop(shop)
                .orderStatus(OrderStatus.AWAITING_CONFIRMATION) // COD starts with awaiting confirmation
                .subtotalAmount(subtotal)
                .shippingFee(shippingFee)
                .notesToSeller(request.getNotesToSeller())
                .orderedAt(OffsetDateTime.now())
                .build();
    }
    
    private void createOrderItems(Order order, List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .productNameSnapshot(variant.getProduct().getName())
                    .variantSkuSnapshot(variant.getSku())
                    .variantOptionsSnapshot(variant.getOptions())
                    .variantImageCloudinaryPublicIdSnapshot(variant.getMainImageCloudinaryPublicId())
                    .productImageCloudinaryPublicIdSnapshot(getProductMainImage(variant.getProduct()))
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPriceAtAddition())
                    .subtotal(cartItem.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .status(OrderItemStatus.PENDING)
                    .build();
            
            orderItemRepository.save(orderItem);
        }
    }
    
    private void createCODPayment(MasterOrder masterOrder, PaymentMethod paymentMethod, BigDecimal amount) {
        Payment payment = Payment.builder()
                .masterOrder(masterOrder)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status(PaymentStatus.PENDING) // COD payment is pending until delivered
                .transactionId("COD-" + masterOrder.getOrderGroupNumber())
                .build();
        
        paymentRepository.save(payment);
    }
    
    private void updateInventoryForOrder(List<CartItem> cartItems) {
        // Decrease stock for each variant in the cart
        for (CartItem cartItem : cartItems) {
            Long variantId = cartItem.getVariant().getId();
            int qty = cartItem.getQuantity();

            int attempts = 0;
            boolean updated = false;
            while (attempts < 3 && !updated) {
                attempts++;
                ProductVariant variant = productVariantRepository.findById(variantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
                int current = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                if (current < qty) {
                    throw new InvalidDataException("Insufficient stock for " + variant.getSku());
                }
                variant.setStockQuantity(current - qty);
                try {
                    productVariantRepository.save(variant);
                    updated = true;
                    log.info("Decreased stock for variant {} by {}. New stock: {}", variant.getId(), qty, variant.getStockQuantity());
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.warn("Optimistic lock conflict on variant {} attempt {}/3", variantId, attempts);
                }
            }
            if (!updated) {
                throw new InvalidDataException("Could not update stock due to concurrent updates. Please try again.");
            }
        }
    }
    
    private void restoreInventoryForOrder(Order order) {
        // Increase stock back for each order item when order is cancelled
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Long variantId = item.getVariant().getId();
            int qty = item.getQuantity();

            int attempts = 0;
            boolean updated = false;
            while (attempts < 3 && !updated) {
                attempts++;
                ProductVariant variant = productVariantRepository.findById(variantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
                int current = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                variant.setStockQuantity(current + qty);
                try {
                    productVariantRepository.save(variant);
                    updated = true;
                    log.info("Restored stock for variant {} by {}. New stock: {}", variant.getId(), qty, variant.getStockQuantity());
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.warn("Optimistic lock conflict on variant {} attempt {}/3", variantId, attempts);
                }
            }
            if (!updated) {
                throw new InvalidDataException("Could not restore stock due to concurrent updates. Please try again.");
            }
        }
    }
    
    private void clearUserCart(Long userId) {
        cartItemRepository.deleteByCartUserId(userId);
        log.info("Cart cleared for user: {}", userId);
    }
    
    private BigDecimal calculateShippingFee(Shop shop, BigDecimal subtotal) {
        // Simple shipping calculation - in real app this would be more complex
        if (subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) { // Free shipping over 500k VND
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(30000); // Default shipping fee 30k VND
    }
    
    private boolean canCancelOrder(OrderStatus status) {
        return status == OrderStatus.PENDING_PAYMENT || 
               status == OrderStatus.AWAITING_CONFIRMATION;
    }
    
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid status transitions
        switch (from) {
            case AWAITING_CONFIRMATION:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED_BY_SELLER;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED_BY_SELLER;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
                return to == OrderStatus.COMPLETED;
            default:
                return false;
        }
    }
    
    private void updateMasterOrderStatus(MasterOrder masterOrder) {
        List<Order> orders = orderRepository.findByMasterOrderIdOrderByCreatedAtDesc(masterOrder.getId());
        
        // Determine overall status based on individual order statuses
        boolean allCompleted = orders.stream().allMatch(o -> o.getOrderStatus() == OrderStatus.COMPLETED);
        boolean anyCancelled = orders.stream().anyMatch(o -> 
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_USER ||
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_SELLER ||
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_ADMIN);
        boolean allCancelled = orders.stream().allMatch(o -> 
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_USER ||
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_SELLER ||
            o.getOrderStatus() == OrderStatus.CANCELLED_BY_ADMIN);
        boolean allAwaiting = orders.stream().allMatch(o -> o.getOrderStatus() == OrderStatus.AWAITING_CONFIRMATION);
        boolean allPendingPayment = orders.stream().allMatch(o -> o.getOrderStatus() == OrderStatus.PENDING_PAYMENT);
        
        if (allPendingPayment) {
            masterOrder.setOverallStatus(MasterOrderStatus.PENDING_PAYMENT);
        } else if (allAwaiting) {
            masterOrder.setOverallStatus(MasterOrderStatus.AWAITING_CONFIRMATION);
        } else if (allCompleted) {
            masterOrder.setOverallStatus(MasterOrderStatus.COMPLETED);
        } else if (allCancelled) {
            masterOrder.setOverallStatus(MasterOrderStatus.CANCELLED);
        } else if (anyCancelled) {
            masterOrder.setOverallStatus(MasterOrderStatus.PARTIALLY_CANCELLED);
        } else {
            masterOrder.setOverallStatus(MasterOrderStatus.PROCESSING);
        }
        
        masterOrderRepository.save(masterOrder);
    }
    
    private MasterOrderResponse buildMasterOrderResponse(MasterOrder masterOrder) {
        List<Order> orders = orderRepository.findByMasterOrderIdOrderByCreatedAtDesc(masterOrder.getId());
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
        
        int totalItems = orders.stream()
                .mapToInt(order -> (int) orderItemRepository.countByOrderId(order.getId()))
                .sum();
        
        int totalQuantity = orders.stream()
                .mapToInt(order -> orderItemRepository.sumQuantityByOrderId(order.getId()))
                .sum();
        
        return MasterOrderResponse.builder()
                .id(masterOrder.getId())
                .orderGroupNumber(masterOrder.getOrderGroupNumber())
                .userId(masterOrder.getUser().getId())
                .customerEmail(masterOrder.getCustomerEmail())
                .customerPhone(masterOrder.getCustomerPhone())
                .shippingAddressSnapshot(masterOrder.getShippingAddressSnapshot())
                .billingAddressSnapshot(masterOrder.getBillingAddressSnapshot())
                .shippingContactName(masterOrder.getShippingContactName())
                .shippingContactPhone(masterOrder.getShippingContactPhone())
                .overallStatus(masterOrder.getOverallStatus())
                .totalAmountPaid(masterOrder.getTotalAmountPaid())
                .totalDiscountAmount(masterOrder.getTotalDiscountAmount())
                .paymentMethodSnapshot(masterOrder.getPaymentMethodSnapshot())
                .createdAt(masterOrder.getCreatedAt())
                .updatedAt(masterOrder.getUpdatedAt())
                .orders(orderResponses)
                .totalOrders(orders.size())
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .grandTotal(masterOrder.getTotalAmountPaid())
                .build();
    }
    
    private OrderResponse buildOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::buildOrderItemResponse)
                .collect(Collectors.toList());
        
        BigDecimal totalAmount = order.getSubtotalAmount()
                .add(order.getShippingFee())
                .subtract(order.getItemDiscountAmount())
                .subtract(order.getShippingDiscountAmount())
                .add(order.getTaxAmount());
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .masterOrderId(order.getMasterOrder().getId())
                .orderGroupNumber(order.getMasterOrder().getOrderGroupNumber())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .orderStatus(order.getOrderStatus())
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .itemDiscountAmount(order.getItemDiscountAmount())
                .shippingDiscountAmount(order.getShippingDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(totalAmount)
                .shippingMethodNameSnapshot(order.getShippingMethodNameSnapshot())
                .notesToSeller(order.getNotesToSeller())
                .orderedAt(order.getOrderedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(itemResponses)
                .totalItems(orderItems.size())
                .totalQuantity(orderItems.stream().mapToInt(OrderItem::getQuantity).sum())
                .build();
    }
    
    private OrderItemResponse buildOrderItemResponse(OrderItem orderItem) {
        String imageUrl = null;
        if (orderItem.getVariantImageCloudinaryPublicIdSnapshot() != null) {
            imageUrl = cloudinaryService.getImageUrl(orderItem.getVariantImageCloudinaryPublicIdSnapshot());
        }
        
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .variantId(orderItem.getVariant().getId())
                .productNameSnapshot(orderItem.getProductNameSnapshot())
                .variantSkuSnapshot(orderItem.getVariantSkuSnapshot())
                .variantOptionsSnapshot(orderItem.getVariantOptionsSnapshot())
                .variantImageUrl(imageUrl)
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .subtotal(orderItem.getSubtotal())
                .status(orderItem.getStatus())
                .build();
    }
    
    private OrderListResponse buildOrderListResponse(Order order) {
        int totalItems = (int) orderItemRepository.countByOrderId(order.getId());
        int totalQuantity = orderItemRepository.sumQuantityByOrderId(order.getId());
        BigDecimal totalAmount = order.getSubtotalAmount().add(order.getShippingFee());
        
        // Get product images from order items with fallback logic
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<String> productImageUrls = orderItems.stream()
                .map(item -> {
                    // Priority 1: Variant image (if available)
                    if (item.getVariantImageCloudinaryPublicIdSnapshot() != null) {
                        return cloudinaryService.getImageUrl(item.getVariantImageCloudinaryPublicIdSnapshot());
                    }
                    // Priority 2: Product image (fallback)
                    if (item.getProductImageCloudinaryPublicIdSnapshot() != null) {
                        return cloudinaryService.getImageUrl(item.getProductImageCloudinaryPublicIdSnapshot());
                    }
                    return null;
                })
                .filter(url -> url != null)
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
        
        return OrderListResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderGroupNumber(order.getMasterOrder().getOrderGroupNumber())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .customerEmail(order.getMasterOrder().getCustomerEmail())
                .orderStatus(order.getOrderStatus())
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .productImageUrls(productImageUrls)
                .orderedAt(order.getOrderedAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    // === HELPER METHODS ===
    
    /**
     * Get the main image URL for a product (first image marked as primary)
     */
    private String getProductMainImage(com.simplecommerce_mdm.product.model.Product product) {
        try {
            // Get product images from ProductImage repository
            List<com.simplecommerce_mdm.product.model.ProductImage> productImages = 
                productImageRepository.findByTargetIdAndTargetType(product.getId(), ImageTargetType.PRODUCT);
            
            // Find primary image first, then fallback to first available image
            Optional<com.simplecommerce_mdm.product.model.ProductImage> primaryImage = 
                productImages.stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst();
            
            if (primaryImage.isPresent()) {
                return primaryImage.get().getCloudinaryPublicId();
            }
            
            // Fallback to first available image
            if (!productImages.isEmpty()) {
                return productImages.get(0).getCloudinaryPublicId();
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Failed to get product main image for product {}: {}", product.getId(), e.getMessage());
            return null;
        }
    }

    // === EMAIL EVENT PUBLISHERS ===

    /**
     * Sends order confirmation email to buyer and new order alerts to sellers
     */
    private void publishOrderEmails(MasterOrder masterOrder) {
        try {
            // Buyer confirmation
            eventPublisher.publishEvent(new OrderEmailEvents.OrderConfirmationEvent(masterOrder));
            // Seller alerts
            for (Order order : masterOrder.getOrders()) {
                eventPublisher.publishEvent(new OrderEmailEvents.NewOrderAlertEvent(order));
            }
        } catch (Exception e) {
            log.error("Failed to publish order email events for group {}: {}", 
                masterOrder.getOrderGroupNumber(), e.getMessage(), e);
        }
    }

    /**
     * Sends order status update email to buyer
     */
    private void publishOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus) {
        try {
            eventPublisher.publishEvent(new OrderEmailEvents.OrderStatusUpdateEvent(order, oldStatus, newStatus));
        } catch (Exception e) {
            log.error("Failed to publish status update email event for order {}: {}", 
                order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    /**
     * Format address snapshot for order storage
     */
    private String formatAddressSnapshot(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getStreetAddress1() != null) {
            sb.append(address.getStreetAddress1());
        }
        if (address.getStreetAddress2() != null && !address.getStreetAddress2().trim().isEmpty()) {
            sb.append(", ").append(address.getStreetAddress2());
        }
        if (address.getWard() != null && !address.getWard().trim().isEmpty()) {
            sb.append(", ").append(address.getWard());
        }
        if (address.getDistrict() != null) {
            sb.append(", ").append(address.getDistrict());
        }
        if (address.getCity() != null) {
            sb.append(", ").append(address.getCity());
        }
        if (address.getPostalCode() != null && !address.getPostalCode().trim().isEmpty()) {
            sb.append(", ").append(address.getPostalCode());
        }
        if (address.getCountryCode() != null) {
            sb.append(", ").append(address.getCountryCode());
        }
        return sb.toString();
    }
} 