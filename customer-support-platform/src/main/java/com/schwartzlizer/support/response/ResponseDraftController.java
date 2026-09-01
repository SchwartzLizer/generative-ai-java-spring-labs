package com.schwartzlizer.support.response;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

/**
 * Exposes AI reply-draft generation and the agent decision on a draft.
 *
 * <p>Mapped at {@code /api/v1} because generation is nested under a feedback while the decision addresses the
 * draft directly. Delegates to {@link ResponseDraftService}.
 */
@RestController
@RequestMapping("/api/v1")
public class ResponseDraftController {
    private final ResponseDraftService service;
    public ResponseDraftController(ResponseDraftService service) { this.service=service; }
    /**
     * Generates a reply draft for a feedback item that has already been analysed.
     *
     * <p>Responds 201 Created with a {@code Location} header of {@code /api/v1/response-drafts/{id}}.
     *
     * @param feedbackId path identifier of the feedback to answer
     * @return 201 Created with the pending draft in the body
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     * @throws AnalysisRequiredException if the feedback has not been analysed yet; surfaces as HTTP 409
     * @throws com.schwartzlizer.support.common.AiProviderException if the AI provider is unavailable; surfaces
     *         as HTTP 503
     */
    @PostMapping("/feedback/{feedbackId}/response-drafts")
    public ResponseEntity<ResponseDraftResponse> generate(@PathVariable("feedbackId") UUID feedbackId) {
        ResponseDraftResponse response=service.generate(feedbackId);
        return ResponseEntity.created(URI.create("/api/v1/response-drafts/"+response.id())).body(response);
    }
    /**
     * Records the agent's approval or rejection of a draft.
     *
     * <p>A draft may be decided only once.
     *
     * @param draftId path identifier of the draft
     * @param request validated payload carrying APPROVED or REJECTED
     * @return the decided draft view
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no draft exists with that id;
     *         surfaces as HTTP 404
     * @throws IllegalArgumentException if the decision is missing or PENDING; surfaces as HTTP 400
     * @throws com.schwartzlizer.support.common.InvalidStateTransitionException if the draft was already decided;
     *         surfaces as HTTP 409
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if a concurrent request decided
     *         the same draft first; surfaces as HTTP 409
     */
    @PatchMapping("/response-drafts/{draftId}/decision")
    public ResponseDraftResponse decide(@PathVariable("draftId") UUID draftId, @Valid @RequestBody DraftDecisionRequest request) { return service.decide(draftId, request.decision()); }
}
