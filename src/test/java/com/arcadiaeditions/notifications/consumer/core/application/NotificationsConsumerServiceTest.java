package com.arcadiaeditions.notifications.consumer.core.application;

import static org.mockito.Mockito.*;

import com.arcadiaeditions.notifications.consumer.config.*;
import com.arcadiaeditions.notifications.consumer.core.application.mappers.*;
import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.dtos.*;
import com.arcadiaeditions.notifications.consumer.core.outbound.jpa.*;
import com.arcadiaeditions.notifications.consumer.infrastructure.jpa.inmemory.*;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acceptance Test for NotificationsConsumerService.
 */
class NotificationsConsumerServiceTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    ServicesInMemoryConfig context = new ServicesInMemoryConfig();
    NotificationsConsumerServiceImpl notificationsConsumerService = context.notificationsConsumerService();

    NotificationRepositoryInMemory notificationRepository = context.notificationRepository();

    @BeforeEach
    void setUp() {
        context.reloadTestData();
    }

    @Test
    void sendOrderConfirmationTest() { // TODO: implement this test
    }

    @Test
    void sendStockUnavailableNotificationTest() { // TODO: implement this test
    }

    @Test
    void sendOrderCancelledNotificationTest() { // TODO: implement this test
    }
}
