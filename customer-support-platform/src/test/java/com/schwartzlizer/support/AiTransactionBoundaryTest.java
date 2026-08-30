package com.schwartzlizer.support;

import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.analysis.FeedbackAnalysisService;
import com.schwartzlizer.support.analysis.Sentiment;
import com.schwartzlizer.support.analysis.SupportCategory;
import com.schwartzlizer.support.analysis.Urgency;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import com.schwartzlizer.support.feedback.FeedbackStatus;
import com.schwartzlizer.support.response.AnalysisRequiredException;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import com.schwartzlizer.support.response.ResponseDraftService;
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
        assertThat(boundaryClient.transactionActive.get()).isFalse();
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
    void missingAnalysisRejectsDraftBeforeProviderInvocation() {
        UUID id = UUID.randomUUID();
        feedbackRepository.save(Feedback.create(id, "C-MISSING", "No analysis yet", Instant.now()));
        boundaryClient.draftCalled.set(false);

        assertThatThrownBy(() -> draftService.generate(id)).isInstanceOf(AnalysisRequiredException.class);

        assertThat(boundaryClient.draftCalled.get()).isFalse();
    }

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        BoundaryClient boundaryClient() {
            return new BoundaryClient();
        }
    }

    static final class BoundaryClient implements CustomerSupportAiClient {
        final AtomicBoolean analysisCalled = new AtomicBoolean();
        final AtomicBoolean draftCalled = new AtomicBoolean();
        final AtomicBoolean transactionActive = new AtomicBoolean();
        volatile boolean failAnalysis;

        @Override
        public FeedbackAnalysisResult analyze(String message) {
            analysisCalled.set(true);
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            if (failAnalysis) throw new RuntimeException("provider failure");
            return new FeedbackAnalysisResult(Sentiment.POSITIVE, SupportCategory.TECHNICAL, Urgency.LOW, "Thank the customer");
        }

        @Override
        public ResponseDraftResult draftResponse(String message, FeedbackAnalysisResult analysis) {
            draftCalled.set(true);
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            return new ResponseDraftResult("Thanks for your feedback.");
        }
    }
}
