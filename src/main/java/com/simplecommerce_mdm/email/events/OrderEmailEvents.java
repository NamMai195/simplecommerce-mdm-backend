package com.simplecommerce_mdm.email.events;

import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.order.model.Order;
import lombok.Value;

public class OrderEmailEvents {

    @Value
    public static class OrderConfirmationEvent {
        MasterOrder masterOrder;
    }

    @Value
    public static class NewOrderAlertEvent {
        Order order;
    }

    @Value
    public static class OrderStatusUpdateEvent {
        Order order;
        String oldStatus;
        String newStatus;
    }
} 