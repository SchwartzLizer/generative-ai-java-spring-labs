package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.*;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Turns a stored feedback message into a persisted AI classification.
 *
 * <p>Analysis results are append-only history: each call adds a row to {@code feedback_analysis} and nothing is
 * overwritten. The same single transaction that appends the row also advances the feedback status.
 *
 * <p>The AI provider is called synchronously inside that transaction, so a slow provider holds the database
 * transaction open.
 */
@Service
public class FeedbackAnalysisService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final CustomerSupportAiClient aiClient; private final AiProviderProperties provider; private final Supplier<UUID> uuidSupplier; private final Clock clock;
    public FeedbackAnalysisService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, CustomerSupportAiClient aiClient, AiProviderProperties provider, Supplier<UUID> uuidSupplier, Clock clock) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.aiClient=aiClient; this.provider=provider; this.uuidSupplier=uuidSupplier; this.clock=clock; }
    /**
     * Classifies the stored feedback message and records the result.
     *
     * <p>The message is sent to the {@link com.schwartzlizer.support.ai.CustomerSupportAiClient} port and the
     * classification is appended to the analysis history as a new row tagged with the configured provider and
     * model. If the feedback is still in status {@code NEW} it is transitioned to {@code ANALYZED} in the same
     * transaction; feedback that has already moved past {@code NEW} keeps its status, so re-running analysis is
     * safe and simply appends another row.
     *
     * <p>The provider call is synchronous and happens inside the transaction, so a slow provider holds the
     * database transaction open. The stored analysis and the status change commit or roll back together.
     *
     * @param feedbackId identifier of the feedback item to classify
     * @return the analysis that was just stored
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     * @throws com.schwartzlizer.support.common.AiProviderException if the provider is unreachable or returns a
     *         payload that cannot be parsed; surfaces as HTTP 503
     */
    @Transactional public FeedbackAnalysisResponse analyze(UUID feedbackId) {
        Feedback feedback=feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysisResult result=aiClient.analyze(feedback.message());
        Instant now=Instant.now(clock);
        FeedbackAnalysis saved=analysisRepository.save(FeedbackAnalysis.create(uuidSupplier.get(), feedback, result.sentiment(), result.category(), result.urgency(), result.recommendedAction(), provider.provider(), provider.model(), now));
        if (feedback.status() == FeedbackStatus.NEW) { feedback.changeStatus(FeedbackStatus.ANALYZED, now); feedbackRepository.save(feedback); }
        return FeedbackAnalysisResponse.from(saved);
    }
}
