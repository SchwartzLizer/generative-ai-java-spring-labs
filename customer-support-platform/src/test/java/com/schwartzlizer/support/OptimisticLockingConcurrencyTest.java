package com.schwartzlizer.support;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import com.schwartzlizer.support.common.OptimisticLockingConflictException;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import com.schwartzlizer.support.feedback.FeedbackResponse;
import com.schwartzlizer.support.feedback.FeedbackService;
import com.schwartzlizer.support.feedback.FeedbackStatus;
import com.schwartzlizer.support.response.DraftDecision;
import com.schwartzlizer.support.response.ResponseDraft;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import com.schwartzlizer.support.response.ResponseDraftResponse;
import com.schwartzlizer.support.response.ResponseDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OptimisticLockingConcurrencyTest {
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ResponseDraftRepository draftRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ResponseDraftService responseDraftService;

    @Test
    void concurrentDecisionsCommitExactlyOneTerminalOutcome() throws Exception {
        Instant now = Instant.parse("2026-08-30T12:00:00Z");
        Feedback feedback = feedbackRepository.saveAndFlush(
            Feedback.create(UUID.randomUUID(), "CUST-CONCURRENT", "Issue", now));
        ResponseDraft draft = draftRepository.saveAndFlush(
            ResponseDraft.create(UUID.randomUUID(), feedback, "Reply", "demo", "v1", now));

        List<Object> outcomes = runConcurrently(
            () -> decide(draft.id(), DraftDecision.APPROVED),
            () -> decide(draft.id(), DraftDecision.REJECTED));

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes).filteredOn(DraftDecision.class::isInstance).hasSize(1);
        assertThat(outcomes).filteredOn(OptimisticLockingConcurrencyTest::isDecisionConflict).hasSize(1);
        assertThat(outcomes).noneMatch(OptimisticLockingFailureException.class::isInstance);
        assertThat(draftRepository.findById(draft.id())).get()
            .extracting(ResponseDraft::decision)
            .isIn(DraftDecision.APPROVED, DraftDecision.REJECTED);
    }

    @Test
    void concurrentStatusChangesNeverLeakOptimisticLockingFailure() throws Exception {
        Instant now = Instant.parse("2026-08-30T12:00:00Z");
        Feedback feedback = feedbackRepository.saveAndFlush(
            Feedback.create(UUID.randomUUID(), "CUST-CONCURRENT", "Issue", now));

        List<Object> outcomes = runConcurrently(
            () -> changeStatus(feedback.id(), FeedbackStatus.IN_PROGRESS),
            () -> changeStatus(feedback.id(), FeedbackStatus.IN_PROGRESS));

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes).filteredOn(FeedbackResponse.class::isInstance).hasSize(1);
        assertThat(outcomes).filteredOn(OptimisticLockingConcurrencyTest::isStatusConflict).hasSize(1);
        assertThat(outcomes).noneMatch(OptimisticLockingFailureException.class::isInstance);
        assertThat(feedbackRepository.findById(feedback.id())).get()
            .extracting(Feedback::status)
            .isEqualTo(FeedbackStatus.IN_PROGRESS);
    }

    private Object decide(UUID draftId, DraftDecision decision) {
        try {
            ResponseDraftResponse response = responseDraftService.decide(draftId, decision);
            return response.decision();
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private Object changeStatus(UUID feedbackId, FeedbackStatus status) {
        try {
            return feedbackService.changeStatus(feedbackId, status);
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private static boolean isDecisionConflict(Object outcome) {
        return outcome instanceof InvalidStateTransitionException
            || outcome instanceof OptimisticLockingConflictException;
    }

    private static boolean isStatusConflict(Object outcome) {
        return outcome instanceof OptimisticLockingConflictException
            || outcome instanceof InvalidStateTransitionException;
    }

    private List<Object> runConcurrently(Callable<Object> first, Callable<Object> second) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Object> firstResult = executor.submit(() -> awaitAndRun(start, first));
            Future<Object> secondResult = executor.submit(() -> awaitAndRun(start, second));
            start.countDown();
            return List.of(
                firstResult.get(10, TimeUnit.SECONDS),
                secondResult.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private Object awaitAndRun(CountDownLatch start, Callable<Object> operation) throws Exception {
        assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
        return operation.call();
    }
}
