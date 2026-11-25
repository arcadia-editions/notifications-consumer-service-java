package com.arcadiaeditions.notifications.consumer.config;

import com.arcadiaeditions.notifications.consumer.core.outbound.jpa.*;
import com.arcadiaeditions.notifications.consumer.infrastructure.jpa.inmemory.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// @Configuration
public class RepositoriesInMemoryConfig {

    protected final NotificationRepository notificationRepository = new NotificationRepositoryInMemory();

    @Bean
    @Primary
    public <T extends NotificationRepository> T notificationRepository() {
        return (T) notificationRepository;
    }
}
