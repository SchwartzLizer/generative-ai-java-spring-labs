package com.schwartzlizer.ai.response;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/response-drafts")
public final class ResponseDraftController {
    private final ResponseDraftService service;

    public ResponseDraftController(ResponseDraftService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResponseDraft> create(@Valid @RequestBody ResponseDraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.issue(), request.tone()));
    }
}
