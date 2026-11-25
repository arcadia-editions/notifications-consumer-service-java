package com.arcadiaeditions.notifications.consumer.infrastructure.jpa.inmemory;

import com.arcadiaeditions.notifications.consumer.core.domain.*;
import com.arcadiaeditions.notifications.consumer.core.outbound.jpa.NotificationRepository;
import java.math.*;
import java.time.*;
import java.util.*;

public class NotificationRepositoryInMemory extends InMemoryJpaRepository<Notification>
        implements NotificationRepository {}
