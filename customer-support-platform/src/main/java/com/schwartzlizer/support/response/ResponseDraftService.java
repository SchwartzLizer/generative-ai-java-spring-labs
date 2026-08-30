package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import com.schwartzlizer.support.analysis.FeedbackAnalysis;
import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.common.OptimisticLockingConflictException;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ResponseDraftService {
    private static final int MAX_DECISION_ATTEMPTS = 3;

    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final ResponseDraftRepository draftRepository;
    private final CustomerSupportAiClient aiClient;
    private final AiProviderProperties provider;
    private final Supplier<UUID> uuidSupplier;
    private final Clock clock;
    private final ResponseDraftDecisionTxOperations decisionTxOperations;

    public ResponseDraftService(
        FeedbackRepository feedbackRepository,
        FeedbackAnalysisRepository analysisRepository,
        ResponseDraftRepository draftRepository,
        CustomerSupportAiClient aiClient,
        AiProviderProperties provider,
        Supplier<UUID> uuidSupplier,
        Clock clock,
        ResponseDraftDecisionTxOperations decisionTxOperations
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisRepository = analysisRepository;
        this.draftRepository = draftRepository;
        this.aiClient = aiClient;
        this.provider = provider;
        this.uuidSupplier = uuidSupplier;
        this.clock = clock;
        this.decisionTxOperations = decisionTxOperations;
    }

    @Transactional
    public ResponseDraftResponse generate(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis analysis = analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(feedbackId);
        if (analysis == null) {
            throw new AnalysisRequiredException();
        }
        ResponseDraftResult result = aiClient.draftResponse(
            feedback.message(),
            new FeedbackAnalysisResult(
                analysis.sentiment(), analysis.category(), analysis.urgency(), analysis.recommendedAction()));
        ResponseDraft saved = draftRepository.save(ResponseDraft.create(
            uuidSupplier.get(), feedback, result.content(), provider.provider(), provider.model(), Instant.now(clock)));
        return ResponseDraftResponse.from(saved);
    }

    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        for (int attempt = 1; attempt <= MAX_DECISION_ATTEMPTS; attempt++) {
            try {
                return decisionTxOperations.decide(draftId, decision);
            } catch (OptimisticLockingFailureException exception) {
                if (attempt == MAX_DECISION_ATTEMPTS) {
                    throw new OptimisticLockingConflictException(
                        "Response draft decision could not be saved because it was modified concurrently",
                        exception);
                }
            }
        }
        throw new IllegalStateException("Unreachable retry state");
    }
}
