package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(FeedbackAnalysisTransactionBoundaryTest.AiProbeConfiguration.class)
class FeedbackAnalysisTransactionBoundaryTest {
    @Autowired
    FeedbackRepository feedbackRepository;

    @Autowired
    FeedbackAnalysisService analysisService;

    @Autowired
    AiProbe probe;

    @Test
    void invokesAiProviderWithoutAnActiveDatabaseTransaction() {
        Feedback feedback = feedbackRepository.saveAndFlush(
            Feedback.create(UUID.randomUUID(), "CUST-TX", "The app crashes", Instant.now())
        );

        analysisService.analyze(feedback.id());

        assertThat(probe.transactionActiveWhenCalled()).isFalse();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AiProbeConfiguration {
        @Bean
        AiProbe aiProbe() {
            return new AiProbe();
        }

        @Bean
        @Primary
        CustomerSupportAiClient customerSupportAiClient(AiProbe probe) {
            return new CustomerSupportAiClient() {
                @Override
                public FeedbackAnalysisResult analyze(String message) {
                    probe.recordTransactionState();
                    return new FeedbackAnalysisResult(
                        Sentiment.NEUTRAL,
                        SupportCategory.GENERAL,
                        Urgency.LOW,
                        "Acknowledge the feedback"
                    );
                }

                @Override
                public ResponseDraftResult draftResponse(String message, FeedbackAnalysisResult analysis) {
                    return new ResponseDraftResult("Thanks for sharing this feedback.");
                }
            };
        }
    }

    static final class AiProbe {
        private boolean transactionActiveWhenCalled;

        void recordTransactionState() {
            transactionActiveWhenCalled = TransactionSynchronizationManager.isActualTransactionActive();
        }

        boolean transactionActiveWhenCalled() {
            return transactionActiveWhenCalled;
        }
    }
}
