package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ResponseDraftService {
    private final ResponseDraftTxOperations txOperations;
    private final CustomerSupportAiClient aiClient;
    private final Clock clock;

    public ResponseDraftService(ResponseDraftTxOperations txOperations,
                                CustomerSupportAiClient aiClient,
                                Clock clock) {
        this.txOperations = txOperations;
        this.aiClient = aiClient;
        this.clock = clock;
    }

    public ResponseDraftResponse generate(UUID feedbackId) {
        ResponseDraftInput input = txOperations.load(feedbackId);
        ResponseDraftResult result = aiClient.draftResponse(input.message(), input.analysis());
        return txOperations.persist(feedbackId, result, Instant.now(clock));
    }

    @org.springframework.transaction.annotation.Transactional
    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        return txOperations.decide(draftId, decision, Instant.now(clock));
    }
}
