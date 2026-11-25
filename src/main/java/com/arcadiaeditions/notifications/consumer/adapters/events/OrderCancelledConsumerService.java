package com.arcadiaeditions.notifications.consumer.adapters.events;

import com.arcadiaeditions.notifications.consumer.asyncapi.client.IOrderCancelledEventV1ConsumerService;
import com.arcadiaeditions.notifications.consumer.core.inbound.NotificationsConsumerService;
import com.arcadiaeditions.orders.checkout.events.avro.OrderCancelled;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: order-cancelled-event-v1 -> NotificationsConsumerService.sendOrderCancelledNotification.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class OrderCancelledConsumerService implements IOrderCancelledEventV1ConsumerService {

    private final NotificationsConsumerService notificationsConsumerService;

    public OrderCancelledConsumerService(NotificationsConsumerService notificationsConsumerService) {
        this.notificationsConsumerService = notificationsConsumerService;
    }

    @Override
    public void onOrderCancelledEventV1(OrderCancelled payload, OrderCancelledHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to SendOrderCancelledNotificationInput and call
        // notificationsConsumerService.sendOrderCancelledNotification
        throw new UnsupportedOperationException("Not implemented");
    }
}
