package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.common.OptimisticLockingConflictException;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackServiceTest {
    @Test
    void retriesStatusChangeFromAFreshTransactionAfterOptimisticConflict() {
        AtomicInteger attempts = new AtomicInteger();
        FeedbackStatusChangeTxOperations operations = new FeedbackStatusChangeTxOperations(null, fixedClock()) {
            @Override
            public FeedbackResponse changeStatus(UUID id, FeedbackStatus status) {
                if (attempts.getAndIncrement() == 0) {
                    throw new OptimisticLockingFailureException("conflict");
                }
                return FeedbackResponse.basic(Feedback.create(id, "CUST-001", "Issue", Instant.now()));
            }
        };
        FeedbackService service = service(operations);

        FeedbackResponse response = service.changeStatus(UUID.randomUUID(), FeedbackStatus.IN_PROGRESS);

        assertThat(response).isNotNull();
        assertThat(attempts).hasValue(2);
    }

    @Test
    void mapsExhaustedStatusChangeConflictsToDedicatedException() {
        AtomicInteger attempts = new AtomicInteger();
        FeedbackStatusChangeTxOperations operations = new FeedbackStatusChangeTxOperations(null, fixedClock()) {
            @Override
            public FeedbackResponse changeStatus(UUID id, FeedbackStatus status) {
                attempts.incrementAndGet();
                throw new OptimisticLockingFailureException("conflict");
            }
        };
        FeedbackService service = service(operations);

        assertThatThrownBy(() -> service.changeStatus(UUID.randomUUID(), FeedbackStatus.IN_PROGRESS))
            .isInstanceOf(OptimisticLockingConflictException.class);
        assertThat(attempts).hasValue(3);
    }

    private FeedbackService service(FeedbackStatusChangeTxOperations operations) {
        return new FeedbackService(
            null,
            (FeedbackAnalysisRepository) null,
            (ResponseDraftRepository) null,
            UUID::randomUUID,
            fixedClock(),
            operations);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-08-30T12:00:00Z"), ZoneOffset.UTC);
    }
}
