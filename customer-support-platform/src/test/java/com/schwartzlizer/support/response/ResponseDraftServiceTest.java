package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.common.InvalidStateTransitionException;
import com.schwartzlizer.support.common.OptimisticLockingConflictException;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseDraftServiceTest {
    @Test
    void retriesDecisionFromAFreshTransactionAfterOptimisticConflict() {
        UUID draftId = UUID.randomUUID();
        AtomicInteger attempts = new AtomicInteger();
        ResponseDraftDecisionTxOperations operations = new ResponseDraftDecisionTxOperations(null, fixedClock()) {
            @Override
            public ResponseDraftResponse decide(UUID id, DraftDecision decision) {
                if (attempts.getAndIncrement() == 0) {
                    throw new OptimisticLockingFailureException("conflict");
                }
                return new ResponseDraftResponse(id, "Reply", decision, "demo", "v1", Instant.now(), null);
            }
        };
        ResponseDraftService service = service(operations);

        ResponseDraftResponse response = service.decide(draftId, DraftDecision.APPROVED);

        assertThat(response.decision()).isEqualTo(DraftDecision.APPROVED);
        assertThat(attempts).hasValue(2);
    }

    @Test
    void mapsExhaustedDecisionConflictsToDedicatedException() {
        AtomicInteger attempts = new AtomicInteger();
        ResponseDraftDecisionTxOperations operations = new ResponseDraftDecisionTxOperations(null, fixedClock()) {
            @Override
            public ResponseDraftResponse decide(UUID id, DraftDecision decision) {
                attempts.incrementAndGet();
                throw new OptimisticLockingFailureException("conflict");
            }
        };
        ResponseDraftService service = service(operations);

        assertThatThrownBy(() -> service.decide(UUID.randomUUID(), DraftDecision.REJECTED))
            .isInstanceOf(OptimisticLockingConflictException.class);
        assertThat(attempts).hasValue(3);
    }

    @Test
    void preservesInvalidStateTransitionWithoutRetry() {
        AtomicInteger attempts = new AtomicInteger();
        ResponseDraftDecisionTxOperations operations = new ResponseDraftDecisionTxOperations(null, fixedClock()) {
            @Override
            public ResponseDraftResponse decide(UUID id, DraftDecision decision) {
                attempts.incrementAndGet();
                throw new InvalidStateTransitionException("Draft has already been decided");
            }
        };
        ResponseDraftService service = service(operations);

        assertThatThrownBy(() -> service.decide(UUID.randomUUID(), DraftDecision.APPROVED))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessage("Draft has already been decided");
        assertThat(attempts).hasValue(1);
    }

    private ResponseDraftService service(ResponseDraftDecisionTxOperations operations) {
        return new ResponseDraftService(
            (FeedbackRepository) null,
            (FeedbackAnalysisRepository) null,
            null,
            (CustomerSupportAiClient) null,
            new AiProviderProperties("demo", "v1"),
            UUID::randomUUID,
            fixedClock(),
            operations);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-08-30T12:00:00Z"), ZoneOffset.UTC);
    }
}
