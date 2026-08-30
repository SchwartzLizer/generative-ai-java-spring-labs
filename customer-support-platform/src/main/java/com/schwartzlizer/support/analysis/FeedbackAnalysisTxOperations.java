package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import com.schwartzlizer.support.feedback.FeedbackStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class FeedbackAnalysisTxOperations {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final AiProviderProperties provider;
    private final Supplier<UUID> uuidSupplier;

    public FeedbackAnalysisTxOperations(
        FeedbackRepository feedbackRepository,
        FeedbackAnalysisRepository analysisRepository,
        AiProviderProperties provider,
        Supplier<UUID> uuidSupplier
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisRepository = analysisRepository;
        this.provider = provider;
        this.uuidSupplier = uuidSupplier;
    }

    @Transactional(readOnly = true)
    public AnalysisInput loadInput(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        return new AnalysisInput(feedback.message());
    }

    @Transactional
    public FeedbackAnalysisResponse persist(UUID feedbackId, FeedbackAnalysisResult result, Instant now) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis saved = analysisRepository.save(
            FeedbackAnalysis.create(
                uuidSupplier.get(),
                feedback,
                result.sentiment(),
                result.category(),
                result.urgency(),
                result.recommendedAction(),
                provider.provider(),
                provider.model(),
                now
            )
        );
        if (feedback.status() == FeedbackStatus.NEW) {
            feedback.changeStatus(FeedbackStatus.ANALYZED, now);
            feedbackRepository.save(feedback);
        }
        return FeedbackAnalysisResponse.from(saved);
    }
}
