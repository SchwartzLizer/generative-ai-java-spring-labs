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

/**
 * Generates AI reply drafts for analysed feedback and records the agent's approve or reject decision.
 *
 * <p>A draft can only be generated for feedback that already has at least one analysis. Both {@code generate}
 * and {@code decide} are transactional.
 *
 * <p>Decide-once is enforced by {@link ResponseDraft} within a transaction and by its {@code @Version} column
 * across concurrent transactions.
 */
@Service
public class ResponseDraftService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final ResponseDraftRepository draftRepository; private final CustomerSupportAiClient aiClient; private final AiProviderProperties provider; private final Supplier<UUID> uuidSupplier; private final Clock clock;
    public ResponseDraftService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, ResponseDraftRepository draftRepository, CustomerSupportAiClient aiClient, AiProviderProperties provider, Supplier<UUID> uuidSupplier, Clock clock) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.draftRepository=draftRepository; this.aiClient=aiClient; this.provider=provider; this.uuidSupplier=uuidSupplier; this.clock=clock; }
    /**
     * Generates and stores a pending reply draft for analysed feedback.
     *
     * <p>The most recent analysis (highest {@code createdAt}) is used as context for the prompt; the stored
     * draft starts in decision {@code PENDING} and records the provider and model that produced it. The
     * feedback status is not changed by this method.
     *
     * @param feedbackId identifier of the feedback to answer
     * @return the newly stored draft, decision {@code PENDING}
     * @throws ResourceNotFoundException if no feedback exists with that id; surfaces as HTTP 404
     * @throws AnalysisRequiredException if the feedback has no analysis yet; surfaces as HTTP 409 (it is a
     *         subtype of InvalidStateTransitionException, so it is a conflict, not a bad request)
     * @throws com.schwartzlizer.support.common.AiProviderException if the AI provider fails or returns an empty
     *         reply; surfaces as HTTP 503
     */
    @Transactional public ResponseDraftResponse generate(UUID feedbackId) {
        Feedback feedback=feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        FeedbackAnalysis analysis=analysisRepository.findTopByFeedback_IdOrderByCreatedAtDesc(feedbackId); if (analysis == null) throw new AnalysisRequiredException();
        ResponseDraftResult result=aiClient.draftResponse(feedback.message(), new FeedbackAnalysisResult(analysis.sentiment(),analysis.category(),analysis.urgency(),analysis.recommendedAction()));
        ResponseDraft saved=draftRepository.save(ResponseDraft.create(uuidSupplier.get(), feedback, result.content(), provider.provider(), provider.model(), Instant.now(clock)));
        return ResponseDraftResponse.from(saved);
    }
    /**
     * Records the agent's approval or rejection of a pending draft.
     *
     * @param draftId identifier of the draft being decided
     * @param decision must be {@code APPROVED} or {@code REJECTED}; {@code PENDING} and null are rejected
     * @return the updated draft, carrying the decision and its timestamp
     * @throws ResourceNotFoundException if no draft exists with that id; surfaces as HTTP 404
     * @throws IllegalArgumentException if decision is null or PENDING; surfaces as HTTP 400
     * @throws com.schwartzlizer.support.common.InvalidStateTransitionException if the draft has already been
     *         decided; surfaces as HTTP 409
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if another transaction decided the
     *         same draft first; raised at commit, surfaces as HTTP 409
     */
    @Transactional public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        ResponseDraft draft=draftRepository.findById(draftId).orElseThrow(() -> new ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        if (decision == DraftDecision.APPROVED) draft.approve(Instant.now(clock)); else draft.reject(Instant.now(clock));
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
