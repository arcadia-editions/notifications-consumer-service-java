package com.arcadiaeditions.notifications.consumer.adapters.events;

import com.arcadiaeditions.notifications.consumer.asyncapi.client.IStockUnavailableEventV1ConsumerService;
import com.arcadiaeditions.notifications.consumer.core.inbound.NotificationsConsumerService;
import com.arcadiaeditions.orders.checkout.events.avro.StockUnavailable;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: stock-unavailable-event-v1 -> NotificationsConsumerService.sendStockUnavailableNotification.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class StockUnavailableConsumerService implements IStockUnavailableEventV1ConsumerService {

    private final NotificationsConsumerService notificationsConsumerService;

    public StockUnavailableConsumerService(NotificationsConsumerService notificationsConsumerService) {
        this.notificationsConsumerService = notificationsConsumerService;
    }

    @Override
    public void onStockUnavailableEventV1(StockUnavailable payload, StockUnavailableHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to SendStockUnavailableNotificationInput and call
        // notificationsConsumerService.sendStockUnavailableNotification
        throw new UnsupportedOperationException("Not implemented");
    }
}
