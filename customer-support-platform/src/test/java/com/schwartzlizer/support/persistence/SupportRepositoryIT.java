package com.schwartzlizer.support.persistence;

import com.schwartzlizer.support.feedback.*;
import com.schwartzlizer.support.response.DraftDecision;
import com.schwartzlizer.support.response.ResponseDraft;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("container")
class SupportRepositoryIT {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired ResponseDraftRepository responseDraftRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Test void persistsFeedbackWithOptimisticVersion() {
        Instant now=Instant.parse("2026-08-28T12:00:00Z"); Feedback saved=feedbackRepository.saveAndFlush(Feedback.create(UUID.randomUUID(),"CUST-001","App crashes",now));
        assertThat(feedbackRepository.findById(saved.id())).get().extracting(Feedback::status).isEqualTo(FeedbackStatus.NEW);
    }

    @Test void rejectsStaleUpdateWithOptimisticLockFailure() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        UUID id = UUID.randomUUID();
        feedbackRepository.saveAndFlush(Feedback.create(id, "CUST-LOCK", "App crashes", now));

        Feedback stale = feedbackRepository.findById(id).orElseThrow();
        Feedback winner = feedbackRepository.findById(id).orElseThrow();

        winner.changeStatus(FeedbackStatus.ANALYZED, now.plusSeconds(1));
        feedbackRepository.saveAndFlush(winner);

        assertThatCode(() -> stale.changeStatus(FeedbackStatus.IN_PROGRESS, now.plusSeconds(2))).doesNotThrowAnyException();

        assertThatThrownBy(() -> feedbackRepository.saveAndFlush(stale)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test void incrementsVersionOnEachCommittedUpdate() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        UUID id = UUID.randomUUID();
        feedbackRepository.saveAndFlush(Feedback.create(id, "CUST-LOCK", "App crashes", now));

        assertThat(feedbackRepository.findById(id).orElseThrow().version()).isEqualTo(0L);

        Feedback loaded = feedbackRepository.findById(id).orElseThrow();
        loaded.changeStatus(FeedbackStatus.ANALYZED, now.plusSeconds(1));
        feedbackRepository.saveAndFlush(loaded);
        assertThat(feedbackRepository.findById(id).orElseThrow().version()).isEqualTo(1L);

        loaded = feedbackRepository.findById(id).orElseThrow();
        loaded.changeStatus(FeedbackStatus.IN_PROGRESS, now.plusSeconds(2));
        feedbackRepository.saveAndFlush(loaded);
        assertThat(feedbackRepository.findById(id).orElseThrow().version()).isEqualTo(2L);
    }

    @Test void keepsWinningWriteAfterOptimisticLockFailure() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        UUID id = UUID.randomUUID();
        feedbackRepository.saveAndFlush(Feedback.create(id, "CUST-LOCK", "App crashes", now));

        Feedback stale = feedbackRepository.findById(id).orElseThrow();
        Feedback winner = feedbackRepository.findById(id).orElseThrow();

        winner.changeStatus(FeedbackStatus.ANALYZED, now.plusSeconds(1));
        feedbackRepository.saveAndFlush(winner);

        assertThatCode(() -> stale.changeStatus(FeedbackStatus.IN_PROGRESS, now.plusSeconds(2))).doesNotThrowAnyException();

        assertThatThrownBy(() -> feedbackRepository.saveAndFlush(stale)).isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Feedback reloaded = feedbackRepository.findById(id).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(FeedbackStatus.ANALYZED);
        assertThat(reloaded.version()).isEqualTo(1L);
    }

    @Test void rejectsStaleDraftDecisionWithOptimisticLockFailure() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        UUID feedbackId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        Feedback feedback = feedbackRepository.saveAndFlush(Feedback.create(feedbackId, "CUST-LOCK", "App crashes", now));
        responseDraftRepository.saveAndFlush(ResponseDraft.create(draftId, feedback, "Draft reply", "demo", "v1", now));

        assertThat(responseDraftRepository.findById(draftId).orElseThrow().version()).isEqualTo(0L);

        ResponseDraft stale = responseDraftRepository.findById(draftId).orElseThrow();
        ResponseDraft winner = responseDraftRepository.findById(draftId).orElseThrow();

        winner.reject(now.plusSeconds(1));
        responseDraftRepository.saveAndFlush(winner);

        assertThatCode(() -> stale.approve(now.plusSeconds(2))).doesNotThrowAnyException();

        assertThatThrownBy(() -> responseDraftRepository.saveAndFlush(stale)).isInstanceOf(ObjectOptimisticLockingFailureException.class);

        ResponseDraft reloaded = responseDraftRepository.findById(draftId).orElseThrow();
        assertThat(reloaded.decision()).isEqualTo(DraftDecision.REJECTED);
        assertThat(reloaded.decidedAt()).isEqualTo(now.plusSeconds(1));
        assertThat(reloaded.version()).isEqualTo(1L);

        assertThat(jdbcTemplate.queryForObject("SELECT version FROM response_draft WHERE id = ?", Long.class, draftId)).isEqualTo(1L);
    }
}
