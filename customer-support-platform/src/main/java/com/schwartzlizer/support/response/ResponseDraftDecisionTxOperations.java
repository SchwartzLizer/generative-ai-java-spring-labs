package com.schwartzlizer.support.response;

import com.schwartzlizer.support.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ResponseDraftDecisionTxOperations {
    private final ResponseDraftRepository draftRepository;
    private final Clock clock;

    public ResponseDraftDecisionTxOperations(ResponseDraftRepository draftRepository, Clock clock) {
        this.draftRepository = draftRepository;
        this.clock = clock;
    }

    @Transactional
    public ResponseDraftResponse decide(UUID draftId, DraftDecision decision) {
        ResponseDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new ResourceNotFoundException("Response draft was not found"));
        if (decision == null || decision == DraftDecision.PENDING) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        Instant now = Instant.now(clock);
        if (decision == DraftDecision.APPROVED) {
            draft.approve(now);
        } else {
            draft.reject(now);
        }
        return ResponseDraftResponse.from(draftRepository.save(draft));
    }
}
