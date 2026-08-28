package com.schwartzlizer.support.feedback;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    private final FeedbackService service;
    public FeedbackController(FeedbackService service) { this.service=service; }
    @PostMapping public ResponseEntity<FeedbackResponse> submit(@Valid @RequestBody SubmitFeedbackRequest request) { FeedbackResponse response=service.submit(request); return ResponseEntity.created(URI.create("/api/v1/feedback/"+response.id())).body(response); }
    @GetMapping public Page<FeedbackResponse> list(@PageableDefault(size=20, sort="createdAt") Pageable pageable) { return service.list(pageable); }
    @GetMapping("/{id}") public FeedbackResponse get(@PathVariable("id") UUID id) { return service.get(id); }
    @PatchMapping("/{id}/status") public FeedbackResponse changeStatus(@PathVariable("id") UUID id, @Valid @RequestBody UpdateFeedbackStatusRequest request) { return service.changeStatus(id, request.status()); }
}
