package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import com.schwartzlizer.support.analysis.FeedbackAnalysis;
import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ResponseDraftTxOperations {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final ResponseDraftRepository draftRepository;
    private final AiProviderProperties provider;
    private final Supplier<UUID> uuidSupplier;

    public ResponseDraftTxOperations(
        FeedbackRepository feedbackRepository,
        FeedbackAnalysisRepository analysisRepository,
        ResponseDraftRepository draftRepository,
        AiProviderProperties provider,
        Supplier<UUID> uuidSupplier
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisRepository = analysisRepository;
        this.draftRepository = draftRepository;
        this.provider = provider;
        this.uuidSupplier = uuidSupplier;
    }

    @Transactional(readOnly = true)
    public ResponseDraftInput loadInput(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis analysis = analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(feedbackId);
        if (analysis == null) {
            throw new AnalysisRequiredException();
        }
        FeedbackAnalysisResult result = new FeedbackAnalysisResult(
            analysis.sentiment(),
            analysis.category(),
            analysis.urgency(),
            analysis.recommendedAction()
        );
        return new ResponseDraftInput(feedback.message(), result);
    }

    @Transactional
    public ResponseDraftResponse persist(UUID feedbackId, ResponseDraftResult result, Instant now) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        ResponseDraft saved = draftRepository.save(
            ResponseDraft.create(
                uuidSupplier.get(),
                feedback,
                result.content(),
                provider.provider(),
                provider.model(),
                now
            )
        );
        return ResponseDraftResponse.from(saved);
    }
}
