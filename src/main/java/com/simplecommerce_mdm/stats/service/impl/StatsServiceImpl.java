package com.simplecommerce_mdm.stats.service.impl;

import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.order.repository.OrderRepository;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ProductRepository;
import com.simplecommerce_mdm.product.repository.ProductVariantRepository;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.stats.dto.*;
import com.simplecommerce_mdm.stats.service.StatsService;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.simplecommerce_mdm.config.BusinessConfigService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    // Business configuration for commission rates
    @Value("${business.commission.admin-rate:0.05}")
    private BigDecimal adminCommissionRate;
    
    @Value("${business.commission.payment-gateway-rate:0.025}")
    private BigDecimal paymentGatewayCommissionRate;

    private final BusinessConfigService businessConfigService;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerStatsOverviewResponse getSellerOverview(CustomUserDetails sellerDetails) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));

        Long shopId = shop.getId();

        // Counts by status
        long pending = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.PENDING_PAYMENT)
                + orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.AWAITING_CONFIRMATION);
        long processing = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.PROCESSING);
        long shipped = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.SHIPPED);
        long delivered = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.DELIVERED);
        long completed = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.CANCELLED_BY_USER)
                + orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.CANCELLED_BY_SELLER)
                + orderRepository.countByShopIdAndOrderStatus(shopId, OrderStatus.CANCELLED_BY_ADMIN);

        long total = pending + processing + shipped + delivered + completed + cancelled;

        // Log business config usage
        businessConfigService.logConfigurationUsage("StatsService", "adminCommissionRate", adminCommissionRate);
        
        // Revenue (sum from completed orders only) - placeholder: using subtotalAmount + shippingFee - discounts where available
        BigDecimal totalRevenue = sumRevenueForShop(shopId, null, null, Collections.singleton(OrderStatus.COMPLETED))
                .multiply(java.math.BigDecimal.ONE.subtract(adminCommissionRate));
        log.info("💰 Seller revenue calculated with commission rate: {}% (seller keeps: {}%)", 
                adminCommissionRate.multiply(BigDecimal.valueOf(100)), 
                BigDecimal.ONE.subtract(adminCommissionRate).multiply(BigDecimal.valueOf(100)));

        // Today revenue
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal todayRevenue = sumRevenueForShop(shopId, start, end, Collections.singleton(OrderStatus.COMPLETED))
                .multiply(java.math.BigDecimal.ONE.subtract(adminCommissionRate));

        return SellerStatsOverviewResponse.builder()
                .shopId(shopId)
                .shopName(shop.getName())
                .totalOrders(total)
                .pendingOrders(pending)
                .processingOrders(processing)
                .shippedOrders(shipped)
                .deliveredOrders(delivered)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesSeriesPoint> getSellerSalesSeries(CustomUserDetails sellerDetails, String range) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));

        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByShopIdAndCreatedAtBetweenAndOrderStatusIn(
                shop.getId(), start, end, Collections.singletonList(OrderStatus.COMPLETED));

        Map<LocalDate, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<SalesSeriesPoint> series = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            List<Order> dayOrders = grouped.getOrDefault(date, Collections.emptyList());
            BigDecimal revenue = dayOrders.stream()
                    .map(this::calculateOrderRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            series.add(new SalesSeriesPoint(date, (long) dayOrders.size(),
                    revenue.multiply(java.math.BigDecimal.ONE.subtract(adminCommissionRate))));
        }
        return series;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductStat> getSellerTopProducts(CustomUserDetails sellerDetails, String sortBy, Integer limit) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));

        List<Object[]> rows = "revenue".equalsIgnoreCase(sortBy)
                ? productRepository.findTopProductsByRevenueForShop(shop.getId())
                : productRepository.findTopProductsByQuantityForShop(shop.getId());

        return rows.stream()
                .limit(limit == null ? 10 : limit)
                .map(r -> new TopProductStat(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        (BigDecimal) r[3]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockItem> getSellerLowStock(CustomUserDetails sellerDetails, Integer threshold) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));

        List<Object[]> rows = productVariantRepository.findLowStockVariantsByShop(shop.getId(), threshold == null ? 5 : threshold);
        return rows.stream()
                .map(r -> new LowStockItem(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[2],
                        ((Number) r[3]).intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakdownEntry> getSellerPaymentBreakdown(CustomUserDetails sellerDetails, String range) {
        // Payment is stored at master order level; here we return system-wide breakdown as placeholder.
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Object[]> rows = paymentRepository.breakdownByMethodAll(start, end);
        return rows.stream().map(r -> new BreakdownEntry((String) r[0], ((Number) r[1]).longValue(), (BigDecimal) r[2]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSellerAvgOrderValue(CustomUserDetails sellerDetails, String range) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.avgOrderValueForShop(shop.getId(), start, end)
                .multiply(java.math.BigDecimal.ONE.subtract(adminCommissionRate));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getSellerReturnsCount(CustomUserDetails sellerDetails, String range) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.countReturnsForShop(shop.getId(), start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakdownEntry> getAdminPaymentBreakdown(String range) {
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Object[]> rows = paymentRepository.breakdownByMethodAll(start, end);
        return rows.stream().map(r -> new BreakdownEntry((String) r[0], ((Number) r[1]).longValue(), (BigDecimal) r[2]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopShopStat> getAdminTopShops(Integer limit) {
        List<Object[]> rows = shopRepository.findTopShopsByRevenue();
        return rows.stream()
                .limit(limit == null ? 10 : limit)
                .map(r -> new TopShopStat(((Number) r[0]).longValue(), (String) r[1], (BigDecimal) r[2]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsOverviewResponse getAdminOverview() {
        // Counts by status across system
        long pending = orderRepository.countByOrderStatus(OrderStatus.PENDING_PAYMENT)
                + orderRepository.countByOrderStatus(OrderStatus.AWAITING_CONFIRMATION);
        long processing = orderRepository.countByOrderStatus(OrderStatus.PROCESSING);
        long shipped = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long completed = orderRepository.countByOrderStatus(OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_USER)
                + orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_SELLER)
                + orderRepository.countByOrderStatus(OrderStatus.CANCELLED_BY_ADMIN);

        long total = pending + processing + shipped + delivered + completed + cancelled;

        // Log business config usage for admin stats
        businessConfigService.logConfigurationUsage("AdminStatsService", "adminCommissionRate", adminCommissionRate);
        
        // Platform revenue = 5% commission of GMV from completed orders
        BigDecimal totalRevenue = sumRevenueForAll(null, null, Collections.singleton(OrderStatus.COMPLETED))
                .multiply(adminCommissionRate);
        log.info("🏛️ Admin commission revenue calculated with rate: {}%", 
                adminCommissionRate.multiply(BigDecimal.valueOf(100)));
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal todayRevenue = sumRevenueForAll(start, end, Collections.singleton(OrderStatus.COMPLETED))
                .multiply(adminCommissionRate);

        // Other totals (placeholders using repos)
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalShops = shopRepository.count();

        return AdminStatsOverviewResponse.builder()
                .totalOrders(total)
                .pendingOrders(pending)
                .processingOrders(processing)
                .shippedOrders(shipped)
                .deliveredOrders(delivered)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalShops(totalShops)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesSeriesPoint> getAdminSalesSeries(String range) {
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByCreatedAtBetweenAndOrderStatusIn(
                start, end, Collections.singletonList(OrderStatus.COMPLETED));

        Map<LocalDate, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<SalesSeriesPoint> series = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            List<Order> dayOrders = grouped.getOrDefault(date, Collections.emptyList());
            BigDecimal revenue = dayOrders.stream()
                    .map(this::calculateOrderRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            series.add(new SalesSeriesPoint(date, (long) dayOrders.size(), revenue.multiply(adminCommissionRate)));
        }
        return series;
    }

    private int parseRangeDays(String range) {
        if (range == null) return 7;
        return switch (range.toLowerCase()) {
            case "30d", "30" -> 30;
            case "7d", "7" -> 7;
            default -> 7;
        };
    }

    private BigDecimal calculateOrderRevenue(Order o) {
        BigDecimal subtotal = Optional.ofNullable(o.getSubtotalAmount()).orElse(BigDecimal.ZERO);
        BigDecimal shipping = Optional.ofNullable(o.getShippingFee()).orElse(BigDecimal.ZERO);
        BigDecimal itemDisc = Optional.ofNullable(o.getItemDiscountAmount()).orElse(BigDecimal.ZERO);
        BigDecimal shipDisc = Optional.ofNullable(o.getShippingDiscountAmount()).orElse(BigDecimal.ZERO);
        BigDecimal tax = Optional.ofNullable(o.getTaxAmount()).orElse(BigDecimal.ZERO);
        return subtotal.add(shipping).add(tax).subtract(itemDisc).subtract(shipDisc);
    }

    private BigDecimal sumRevenueForShop(Long shopId, LocalDateTime start, LocalDateTime end, Collection<OrderStatus> statuses) {
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByShopIdAndCreatedAtBetweenAndOrderStatusIn(shopId, start, end, statuses);
        } else {
            orders = orderRepository.findByOrderStatusIn(statuses).stream()
                    .filter(o -> o.getShop().getId().equals(shopId))
                    .collect(Collectors.toList());
        }
        return orders.stream().map(this::calculateOrderRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumRevenueForAll(LocalDateTime start, LocalDateTime end, Collection<OrderStatus> statuses) {
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByCreatedAtBetweenAndOrderStatusIn(start, end, statuses);
        } else {
            orders = orderRepository.findByOrderStatusIn(statuses);
        }
        return orders.stream().map(this::calculateOrderRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


