package com.arcadiaeditions.notifications.consumer.core.application.mappers;

import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {BaseMapper.class})
public interface EventsMapper {

    EventsMapper INSTANCE = Mappers.getMapper(EventsMapper.class);

    com.arcadiaeditions.notifications.consumer.events.avro.NotificationChannel asNotificationChannel(
            NotificationChannel notificationChannel);

    com.arcadiaeditions.notifications.consumer.events.avro.NotificationDeliveryFailed asNotificationDeliveryFailed(
            Notification notification);

    com.arcadiaeditions.notifications.consumer.events.avro.NotificationType asNotificationType(
            NotificationType notificationType);

    com.arcadiaeditions.notifications.consumer.events.avro.StockUnavailableNotificationSent
            asStockUnavailableNotificationSent(Notification notification);

    com.arcadiaeditions.notifications.consumer.events.avro.OrderConfirmationSent asOrderConfirmationSent(
            Notification notification);

    com.arcadiaeditions.notifications.consumer.events.avro.OrderCancelledNotificationSent
            asOrderCancelledNotificationSent(Notification notification);
}
