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

@Service
public class FeedbackAnalysisService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final CustomerSupportAiClient aiClient; private final AiProviderProperties provider; private final Supplier<UUID> uuidSupplier; private final Clock clock;
    public FeedbackAnalysisService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, CustomerSupportAiClient aiClient, AiProviderProperties provider, Supplier<UUID> uuidSupplier, Clock clock) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.aiClient=aiClient; this.provider=provider; this.uuidSupplier=uuidSupplier; this.clock=clock; }
    @Transactional public FeedbackAnalysisResponse analyze(UUID feedbackId) {
        Feedback feedback=feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysisResult result=aiClient.analyze(feedback.message());
        Instant now=Instant.now(clock);
        FeedbackAnalysis saved=analysisRepository.save(FeedbackAnalysis.create(uuidSupplier.get(), feedback, result.sentiment(), result.category(), result.urgency(), result.recommendedAction(), provider.provider(), provider.model(), now));
        if (feedback.status() == FeedbackStatus.NEW) { feedback.changeStatus(FeedbackStatus.ANALYZED, now); feedbackRepository.save(feedback); }
        return FeedbackAnalysisResponse.from(saved);
    }
}
