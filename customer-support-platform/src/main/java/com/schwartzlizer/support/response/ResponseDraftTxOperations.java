package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.analysis.FeedbackAnalysis;
import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class ResponseDraftTxOperations {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final ResponseDraftRepository draftRepository;
    private final AiProviderProperties provider;
    private final Supplier<UUID> uuidSupplier;
    private final Clock clock;

    public ResponseDraftTxOperations(FeedbackRepository feedbackRepository,
                                     FeedbackAnalysisRepository analysisRepository,
                                     ResponseDraftRepository draftRepository,
                                     AiProviderProperties provider,
                                     Supplier<UUID> uuidSupplier,
                                     Clock clock) {
        this.feedbackRepository = feedbackRepository;
        this.analysisRepository = analysisRepository;
        this.draftRepository = draftRepository;
        this.provider = provider;
        this.uuidSupplier = uuidSupplier;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ResponseDraftInput load(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis analysis = analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(feedbackId);
        if (analysis == null) {
            throw new AnalysisRequiredException();
        }
        return new ResponseDraftInput(feedback.id(), feedback.message(), new FeedbackAnalysisResult(
            analysis.sentiment(), analysis.category(), analysis.urgency(), analysis.recommendedAction()));
    }

    @Transactional
    public ResponseDraftResponse persist(UUID feedbackId, ResponseDraftResult result, Instant now) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        ResponseDraft draft = draftRepository.save(ResponseDraft.create(
            uuidSupplier.get(), feedback, result.content(), provider.provider(), provider.model(), now));
        return ResponseDraftResponse.from(draft);
    }

    @Transactional
    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision, Instant now) {
        ResponseDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        if (decision == DraftDecision.APPROVED) {
            draft.approve(now);
        } else {
            draft.reject(now);
        }
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
