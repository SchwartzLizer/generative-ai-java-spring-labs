package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.ResponseDraftResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

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

    public ResponseDraftResponse generate(UUID feedbackId) {
        ResponseDraftInput input = txOperations.loadInput(feedbackId);
        ResponseDraftResult result = aiClient.draftResponse(input.message(), input.analysis());
        Instant now = Instant.now(clock);
        return txOperations.persist(feedbackId, result, now);
    }

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
