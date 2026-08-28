package com.schwartzlizer.support.analysis;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback/{feedbackId}/analyses")
public class FeedbackAnalysisController {
    private final FeedbackAnalysisService service;
    public FeedbackAnalysisController(FeedbackAnalysisService service) { this.service=service; }
    @PostMapping public ResponseEntity<FeedbackAnalysisResponse> analyze(@PathVariable("feedbackId") UUID feedbackId) { FeedbackAnalysisResponse response=service.analyze(feedbackId); return ResponseEntity.created(URI.create("/api/v1/feedback/"+feedbackId+"/analyses/"+response.id())).body(response); }
}
