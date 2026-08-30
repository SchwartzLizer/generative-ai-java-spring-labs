package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.schwartzlizer.support.common.ResourceNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ResponseDraftService {
    private final ResponseDraftTxOperations txOperations;
    private final CustomerSupportAiClient aiClient;
    private final ResponseDraftRepository draftRepository;
    private final Clock clock;

    public ResponseDraftService(ResponseDraftTxOperations txOperations,
                                CustomerSupportAiClient aiClient,
                                ResponseDraftRepository draftRepository,
                                Clock clock) {
        this.txOperations = txOperations;
        this.aiClient = aiClient;
        this.draftRepository = draftRepository;
        this.clock = clock;
    }

    public ResponseDraftResponse generate(UUID feedbackId) {
        ResponseDraftInput input = txOperations.load(feedbackId);
        ResponseDraftResult result = aiClient.draftResponse(input.message(), input.analysis());
        return txOperations.persist(feedbackId, result, Instant.now(clock));
    }

    @Transactional
    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        ResponseDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        if (decision == DraftDecision.APPROVED) draft.approve(Instant.now(clock));
        else draft.reject(Instant.now(clock));
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
