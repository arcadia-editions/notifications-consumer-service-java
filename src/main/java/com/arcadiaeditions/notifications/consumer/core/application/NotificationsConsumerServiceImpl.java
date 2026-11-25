package com.arcadiaeditions.notifications.consumer.core.application;

import com.arcadiaeditions.notifications.consumer.asyncapi.provider.*;
import com.arcadiaeditions.notifications.consumer.core.application.mappers.*;
import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.dtos.*;
import com.arcadiaeditions.notifications.consumer.core.outbound.jpa.*;
// import com.arcadiaeditions.notifications.consumer.events.avro.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing [Notification].
 */
@Service
@Transactional
@org.jspecify.annotations.NullMarked
public class NotificationsConsumerServiceImpl implements NotificationsConsumerService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NotificationsConsumerServiceMapper notificationsConsumerServiceMapper =
            NotificationsConsumerServiceMapper.INSTANCE;

    private final NotificationRepository notificationRepository;

    private final EventsMapper eventsMapper = EventsMapper.INSTANCE;
    private final NotificationsConsumerEventsProducer eventsProducer;

    /**
     * Constructor.
     */
    public NotificationsConsumerServiceImpl(
            NotificationRepository notificationRepository, NotificationsConsumerEventsProducer eventsProducer) {
        this.notificationRepository = notificationRepository;
        this.eventsProducer = eventsProducer;
    }

    public Notification sendOrderConfirmation(SendOrderConfirmationInput input) {

        log.debug("Request sendOrderConfirmation: {}", input);

        var notification = notificationsConsumerServiceMapper.update(new Notification(), input);
        // TODO: implement this method
        // emit events
        var orderConfirmationSent = eventsMapper.asOrderConfirmationSent(notification);
        eventsProducer.onOrderConfirmationSent(orderConfirmationSent);
        var notificationDeliveryFailed = eventsMapper.asNotificationDeliveryFailed(notification);
        eventsProducer.onNotificationDeliveryFailed(notificationDeliveryFailed);
        return notification;
    }

    public Notification sendStockUnavailableNotification(SendStockUnavailableNotificationInput input) {

        log.debug("Request sendStockUnavailableNotification: {}", input);

        var notification = notificationsConsumerServiceMapper.update(new Notification(), input);
        // TODO: implement this method
        // emit events
        var stockUnavailableNotificationSent = eventsMapper.asStockUnavailableNotificationSent(notification);
        eventsProducer.onStockUnavailableNotificationSent(stockUnavailableNotificationSent);
        var notificationDeliveryFailed = eventsMapper.asNotificationDeliveryFailed(notification);
        eventsProducer.onNotificationDeliveryFailed(notificationDeliveryFailed);
        return notification;
    }

    public Notification sendOrderCancelledNotification(SendOrderCancelledNotificationInput input) {

        log.debug("Request sendOrderCancelledNotification: {}", input);

        var notification = notificationsConsumerServiceMapper.update(new Notification(), input);
        // TODO: implement this method
        // emit events
        var orderCancelledNotificationSent = eventsMapper.asOrderCancelledNotificationSent(notification);
        eventsProducer.onOrderCancelledNotificationSent(orderCancelledNotificationSent);
        var notificationDeliveryFailed = eventsMapper.asNotificationDeliveryFailed(notification);
        eventsProducer.onNotificationDeliveryFailed(notificationDeliveryFailed);
        return notification;
    }
}
