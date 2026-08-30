package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Turns a stored feedback message into a persisted AI classification.
 *
 * <p>Analysis results are append-only history: each call adds a row to {@code feedback_analysis} and nothing is
 * overwritten. The same single transaction that appends the row also advances the feedback status.
 *
 * <p>The AI provider call happens outside the database transaction: the feedback message is loaded, the
 * provider is called, and the result is persisted as three separate steps, so a slow provider no longer holds
 * a database transaction open.
 */
@Service
public class FeedbackAnalysisService {
    private final FeedbackAnalysisTxOperations txOperations;
    private final CustomerSupportAiClient aiClient;
    private final Clock clock;
    private final Timer aiProviderCallDuration;

    public FeedbackAnalysisService(
        FeedbackAnalysisTxOperations txOperations,
        CustomerSupportAiClient aiClient,
        Clock clock,
        AiProviderProperties provider,
        MeterRegistry meterRegistry
    ) {
        this.txOperations = txOperations;
        this.aiClient = aiClient;
        this.clock = clock;
        this.aiProviderCallDuration = Timer.builder("ai.provider.call.duration")
            .description("AI provider call duration")
            .tag("provider", provider.provider())
            .register(meterRegistry);
    }

    /**
     * Classifies the stored feedback message and records the result.
     *
     * <p>The message is sent to the {@link com.schwartzlizer.support.ai.CustomerSupportAiClient} port and the
     * classification is appended to the analysis history as a new row tagged with the configured provider and
     * model. If the feedback is still in status {@code NEW} it is transitioned to {@code ANALYZED} in the same
     * transaction; feedback that has already moved past {@code NEW} keeps its status, so re-running analysis is
     * safe and simply appends another row.
     *
     * <p>The feedback message is loaded in one transaction, the provider is called with no transaction open,
     * and the classification plus status change are persisted together in a second transaction. The stored
     * analysis and the status change still commit or roll back together, but a slow provider no longer holds a
     * database transaction open.
     *
     * @param feedbackId identifier of the feedback item to classify
     * @return the analysis that was just stored
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     * @throws com.schwartzlizer.support.common.AiProviderException if the provider is unreachable or returns a
     *         payload that cannot be parsed; surfaces as HTTP 503
     */
    public FeedbackAnalysisResponse analyze(UUID feedbackId) {
        AnalysisInput input = txOperations.loadInput(feedbackId);
        FeedbackAnalysisResult result = aiProviderCallDuration.record(() -> aiClient.analyze(input.message()));
        Instant now = Instant.now(clock);
        return txOperations.persist(feedbackId, result, now);
    }
}
