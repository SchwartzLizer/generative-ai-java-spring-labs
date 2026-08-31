package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Generates AI reply drafts for analysed feedback and records the agent's approve or reject decision.
 *
 * <p>A draft can only be generated for feedback that already has at least one analysis.
 *
 * <p>{@code decide} runs in a single transaction. {@code generate} does not: it loads the feedback and its
 * most recent analysis in one transaction, calls the AI provider with no transaction open, and persists the
 * draft in a second transaction, so a slow provider does not hold a database transaction open.
 *
 * <p>Decide-once is enforced by {@link ResponseDraft} within a transaction and by its {@code @Version} column
 * across concurrent transactions.
 */
@Service
public class ResponseDraftService {
    private final ResponseDraftTxOperations txOperations;
    private final ResponseDraftRepository draftRepository;
    private final CustomerSupportAiClient aiClient;
    private final Clock clock;

    public ResponseDraftService(
        ResponseDraftTxOperations txOperations,
        ResponseDraftRepository draftRepository,
        CustomerSupportAiClient aiClient,
        Clock clock
    ) {
        this.txOperations = txOperations;
        this.draftRepository = draftRepository;
        this.aiClient = aiClient;
        this.clock = clock;
    }

    /**
     * Generates and stores a pending reply draft for analysed feedback.
     *
     * <p>The most recent analysis (highest {@code createdAt}) is used as context for the prompt; the stored
     * draft starts in decision {@code PENDING} and records the provider and model that produced it. The
     * feedback status is not changed by this method.
     *
     * <p>The feedback and its most recent analysis are loaded in one transaction, the AI provider is called
     * with no transaction open, and the draft is persisted in a second transaction.
     *
     * @param feedbackId identifier of the feedback to answer
     * @return the newly stored draft, decision {@code PENDING}
     * @throws ResourceNotFoundException if no feedback exists with that id; surfaces as HTTP 404
     * @throws AnalysisRequiredException if the feedback has no analysis yet; surfaces as HTTP 409 (it is a
     *         subtype of InvalidStateTransitionException, so it is a conflict, not a bad request)
     * @throws com.schwartzlizer.support.common.AiProviderException if the AI provider fails or returns an empty
     *         reply; surfaces as HTTP 503
     */
    public ResponseDraftResponse generate(UUID feedbackId) {
        ResponseDraftInput input = txOperations.loadInput(feedbackId);
        ResponseDraftResult result = aiClient.draftResponse(input.message(), input.analysis());
        Instant now = Instant.now(clock);
        return txOperations.persist(feedbackId, result, now);
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
    @Transactional
    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        ResponseDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new com.schwartzlizer.support.common.ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        if (decision == DraftDecision.APPROVED) {
            draft.approve(Instant.now(clock));
        } else {
            draft.reject(Instant.now(clock));
        }
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
