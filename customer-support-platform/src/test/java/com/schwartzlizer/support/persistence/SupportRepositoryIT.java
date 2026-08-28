package com.schwartzlizer.support.persistence;

import com.schwartzlizer.support.feedback.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("container")
class SupportRepositoryIT {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired FeedbackRepository feedbackRepository;
    @Test void persistsFeedbackWithOptimisticVersion() {
        Instant now=Instant.parse("2026-08-28T12:00:00Z"); Feedback saved=feedbackRepository.saveAndFlush(Feedback.create(UUID.randomUUID(),"CUST-001","App crashes",now));
        assertThat(feedbackRepository.findById(saved.id())).get().extracting(Feedback::status).isEqualTo(FeedbackStatus.NEW);
    }
}
