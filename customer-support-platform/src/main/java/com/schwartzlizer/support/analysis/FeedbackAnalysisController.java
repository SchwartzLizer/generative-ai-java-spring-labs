package com.schwartzlizer.support.analysis;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

/**
 * Exposes AI analysis of a feedback item under {@code /api/v1/feedback/{feedbackId}/analyses}.
 *
 * <p>Analyses are append-only history, so POSTing repeatedly creates additional analyses rather than replacing
 * one. Delegates to {@link FeedbackAnalysisService}.
 */
@RestController
@RequestMapping("/api/v1/feedback/{feedbackId}/analyses")
public class FeedbackAnalysisController {
    private final FeedbackAnalysisService service;
    public FeedbackAnalysisController(FeedbackAnalysisService service) { this.service=service; }
    /**
     * Runs AI analysis on one feedback item and stores the result.
     *
     * <p>Responds 201 Created with a {@code Location} header pointing at the new analysis. The first analysis of
     * a feedback still in {@code NEW} also moves it to {@code ANALYZED}.
     *
     * @param feedbackId path identifier of the feedback to analyse
     * @return 201 Created with the stored analysis in the body
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     * @throws com.schwartzlizer.support.common.AiProviderException if the AI provider is unavailable or returns
     *         an unusable payload; surfaces as HTTP 503
     */
    @PostMapping
    public ResponseEntity<FeedbackAnalysisResponse> analyze(@PathVariable("feedbackId") UUID feedbackId) {
        FeedbackAnalysisResponse response=service.analyze(feedbackId);
        return ResponseEntity.created(URI.create("/api/v1/feedback/"+feedbackId+"/analyses/"+response.id())).body(response);
    }
}
