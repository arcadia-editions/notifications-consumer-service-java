package com.arcadiaeditions.notifications.consumer.infrastructure.jpa;

import com.arcadiaeditions.notifications.consumer.config.DockerComposeInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DockerComposeInitializer.EnableDockerCompose
@org.springframework.transaction.annotation.Transactional
public abstract class BaseRepositoryIntegrationTest {}
