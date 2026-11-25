package com.arcadiaeditions.notifications.consumer.config;

import com.arcadiaeditions.notifications.consumer.asyncapi.provider.*;
import com.arcadiaeditions.notifications.consumer.core.application.*;
import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.inbound.*;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Services InMemory Config. It can be used standalone or with @SpringBootTest.
 */
@Configuration
@Profile("in-memory")
public class ServicesInMemoryConfig extends RepositoriesInMemoryConfig {

    protected final EventsProducerInMemoryContext eventsProducerInMemoryContext = new EventsProducerInMemoryContext();

    protected final NotificationsConsumerServiceImpl notificationsConsumerService =
            new NotificationsConsumerServiceImpl(
                    notificationRepository(), eventsProducerInMemoryContext.notificationsConsumerEventsProducer());

    @Bean
    public <T extends NotificationsConsumerService> T notificationsConsumerService() {
        return (T) notificationsConsumerService;
    }

    static List<Notification> _notifications;

    public void reloadTestData() {

        var testDataLoader = new TestDataLoader(List.of(Notification.class));
        var notifications = _notifications != null
                ? _notifications
                : testDataLoader.loadCollectionTestDataAsObjects(Notification.class);
        notificationRepository().deleteAll();
        notificationRepository().saveAll(notifications);
    }

    public EventsProducerInMemoryContext getEventsProducerInMemoryContext() {
        return eventsProducerInMemoryContext;
    }
}
