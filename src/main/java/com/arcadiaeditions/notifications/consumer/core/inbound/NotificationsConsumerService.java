package com.arcadiaeditions.notifications.consumer.core.inbound;

import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Inbound Service Port for managing [Notification].
 */
@org.jspecify.annotations.NullMarked
public interface NotificationsConsumerService {

    /**
     *
     *
     * With Events: [[OrderConfirmationSent, NotificationDeliveryFailed]].
     *
     */
    public Notification sendOrderConfirmation(SendOrderConfirmationInput input);
    /**
     *
     *
     * With Events: [[StockUnavailableNotificationSent, NotificationDeliveryFailed]].
     *
     */
    public Notification sendStockUnavailableNotification(SendStockUnavailableNotificationInput input);
    /**
     *
     *
     * With Events: [[OrderCancelledNotificationSent, NotificationDeliveryFailed]].
     *
     */
    public Notification sendOrderCancelledNotification(SendOrderCancelledNotificationInput input);
}
