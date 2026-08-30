package com.schwartzlizer.support;

import com.schwartzlizer.support.ai.*;
import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.*;
import com.schwartzlizer.support.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AiTransactionBoundaryTest {
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired FeedbackAnalysisRepository analysisRepository;
    @Autowired ResponseDraftRepository draftRepository;
    @Autowired FeedbackAnalysisService analysisService;
    @Autowired ResponseDraftService draftService;
    @Autowired BoundaryClient boundaryClient;

    @Test
    void providerInvocationsRunWithoutAnActiveTransaction() {
        UUID id = UUID.randomUUID();
        feedbackRepository.save(Feedback.create(id, "C-BOUNDARY", "The app is helpful", Instant.now()));
        analysisService.analyze(id);
        draftService.generate(id);
        assertThat(boundaryClient.analysisCalled.get()).isTrue();
        assertThat(boundaryClient.draftCalled.get()).isTrue();
        assertThat(boundaryClient.analysisTransactionActive.get()).isFalse();
        assertThat(boundaryClient.draftTransactionActive.get()).isFalse();
    }

    @Test
    void providerFailureAfterLoadDoesNotPersistAnalysisOrStatus() {
        UUID id = UUID.randomUUID();
        feedbackRepository.save(Feedback.create(id, "C-FAIL", "The app is broken", Instant.now()));
        boundaryClient.failAnalysis = true;
        assertThatThrownBy(() -> analysisService.analyze(id)).isInstanceOf(RuntimeException.class);
        assertThat(analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(id)).isNull();
        assertThat(feedbackRepository.findById(id).orElseThrow().status()).isEqualTo(FeedbackStatus.NEW);
        boundaryClient.failAnalysis = false;
    }

    @Test
    void draftProviderFailureAfterLoadDoesNotPersistDraft() {
        UUID id = UUID.randomUUID();
        feedbackRepository.save(Feedback.create(id, "C-DRAFT-FAIL", "The app is helpful", Instant.now()));
        analysisService.analyze(id);
        long before = draftRepository.count();
        boundaryClient.failDraft = true;
        assertThatThrownBy(() -> draftService.generate(id)).isInstanceOf(RuntimeException.class);
        assertThat(draftRepository.count()).isEqualTo(before);
        boundaryClient.failDraft = false;
    }

    @Test
    void missingAnalysisRejectsDraftBeforeProviderInvocation() {
        UUID id = UUID.randomUUID();
        feedbackRepository.save(Feedback.create(id, "C-MISSING", "No analysis yet", Instant.now()));
        boundaryClient.draftCalled.set(false);
        assertThatThrownBy(() -> draftService.generate(id)).isInstanceOf(AnalysisRequiredException.class);
        assertThat(boundaryClient.draftCalled.get()).isFalse();
    }

    @TestConfiguration
    static class Config {
        @Bean @Primary BoundaryClient boundaryClient() { return new BoundaryClient(); }
    }

    static final class BoundaryClient implements CustomerSupportAiClient {
        final AtomicBoolean analysisCalled = new AtomicBoolean();
        final AtomicBoolean draftCalled = new AtomicBoolean();
        final AtomicBoolean analysisTransactionActive = new AtomicBoolean();
        final AtomicBoolean draftTransactionActive = new AtomicBoolean();
        volatile boolean failAnalysis;
        volatile boolean failDraft;
        @Override public FeedbackAnalysisResult analyze(String message) {
            analysisCalled.set(true);
            analysisTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            if (failAnalysis) throw new RuntimeException("provider failure");
            return new FeedbackAnalysisResult(Sentiment.POSITIVE, SupportCategory.TECHNICAL, Urgency.LOW, "Thank the customer");
        }
        @Override public ResponseDraftResult draftResponse(String message, FeedbackAnalysisResult analysis) {
            draftCalled.set(true);
            draftTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            if (failDraft) throw new RuntimeException("provider failure");
            return new ResponseDraftResult("Thanks for your feedback.");
        }
    }
}
