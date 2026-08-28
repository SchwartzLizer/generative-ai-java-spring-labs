package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.*;
import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.feedback.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ResponseDraftService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final ResponseDraftRepository draftRepository; private final CustomerSupportAiClient aiClient; private final AiProviderProperties provider; private final Supplier<UUID> uuidSupplier; private final Clock clock;
    public ResponseDraftService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, ResponseDraftRepository draftRepository, CustomerSupportAiClient aiClient, AiProviderProperties provider, Supplier<UUID> uuidSupplier, Clock clock) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.draftRepository=draftRepository; this.aiClient=aiClient; this.provider=provider; this.uuidSupplier=uuidSupplier; this.clock=clock; }
    @Transactional public ResponseDraftResponse generate(UUID feedbackId) {
        Feedback feedback=feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis analysis=analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(feedbackId); if (analysis == null) throw new AnalysisRequiredException();
        ResponseDraftResult result=aiClient.draftResponse(feedback.message(), new FeedbackAnalysisResult(analysis.sentiment(),analysis.category(),analysis.urgency(),analysis.recommendedAction()));
        ResponseDraft saved=draftRepository.save(ResponseDraft.create(uuidSupplier.get(), feedback, result.content(), provider.provider(), provider.model(), Instant.now(clock)));
        return ResponseDraftResponse.from(saved);
    }
    @Transactional public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        ResponseDraft draft=draftRepository.findById(draftId).orElseThrow(() -> new ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        if (decision == DraftDecision.APPROVED) draft.approve(Instant.now(clock)); else draft.reject(Instant.now(clock));
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
