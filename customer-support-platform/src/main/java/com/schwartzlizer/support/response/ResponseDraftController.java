package com.schwartzlizer.support.response;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ResponseDraftController {
    private final ResponseDraftService service;
    public ResponseDraftController(ResponseDraftService service) { this.service=service; }
    @PostMapping("/feedback/{feedbackId}/response-drafts") public ResponseEntity<ResponseDraftResponse> generate(@PathVariable("feedbackId") UUID feedbackId) { ResponseDraftResponse response=service.generate(feedbackId); return ResponseEntity.created(URI.create("/api/v1/response-drafts/"+response.id())).body(response); }
    @PatchMapping("/response-drafts/{draftId}/decision") public ResponseDraftResponse decide(@PathVariable("draftId") UUID draftId, @Valid @RequestBody DraftDecisionRequest request) { return service.decide(draftId, request.decision()); }
}
