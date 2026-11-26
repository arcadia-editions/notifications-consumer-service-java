package com.arcadiaeditions.notifications.consumer.config;

import io.zenwave360.modulith.events.scs.config.EnableSpringCloudStreamEventExternalization;
import org.springframework.context.annotation.Configuration;

/**
 * Transactional outbox (docs/adr/0003): the generated producers publish {@code Message<?>} events through
 * {@code ApplicationEventPublisher}; Spring Modulith stores them in the event publication registry in the same
 * transaction and this externalizer sends them to their Spring Cloud Stream binding after commit.
 */
@Configuration
@EnableSpringCloudStreamEventExternalization
public class EventExternalizationConfiguration {}
