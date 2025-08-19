package com.simplecommerce_mdm.stats.service.impl;

import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.order.repository.OrderRepository;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ProductRepository;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.stats.dto.*;
import com.simplecommerce_mdm.stats.service.StatsService;
import com.simplecommerce_mdm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

        // Revenue (sum from completed orders only) - placeholder: using subtotalAmount + shippingFee - discounts where available
        BigDecimal totalRevenue = sumRevenueForShop(shopId, null, null, Collections.singleton(OrderStatus.COMPLETED));

        // Today revenue
        LocalDate today = LocalDate.now();
        OffsetDateTime start = today.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = start.plusDays(1);
        BigDecimal todayRevenue = sumRevenueForShop(shopId, start, end, Collections.singleton(OrderStatus.COMPLETED));

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
        OffsetDateTime start = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = today.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

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
            series.add(new SalesSeriesPoint(date, (long) dayOrders.size(), revenue));
        }
        return series;
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

        // Revenue
        BigDecimal totalRevenue = sumRevenueForAll(null, null, Collections.singleton(OrderStatus.COMPLETED));
        LocalDate today = LocalDate.now();
        OffsetDateTime start = today.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = start.plusDays(1);
        BigDecimal todayRevenue = sumRevenueForAll(start, end, Collections.singleton(OrderStatus.COMPLETED));

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
        OffsetDateTime start = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = today.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

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
            series.add(new SalesSeriesPoint(date, (long) dayOrders.size(), revenue));
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

    private BigDecimal sumRevenueForShop(Long shopId, OffsetDateTime start, OffsetDateTime end, Collection<OrderStatus> statuses) {
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

    private BigDecimal sumRevenueForAll(OffsetDateTime start, OffsetDateTime end, Collection<OrderStatus> statuses) {
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByCreatedAtBetweenAndOrderStatusIn(start, end, statuses);
        } else {
            orders = orderRepository.findByOrderStatusIn(statuses);
        }
        return orders.stream().map(this::calculateOrderRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


