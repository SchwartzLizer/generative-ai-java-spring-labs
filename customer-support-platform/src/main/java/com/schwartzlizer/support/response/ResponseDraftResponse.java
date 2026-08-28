package com.schwartzlizer.support.response;

import java.time.Instant;
import java.util.UUID;

public record ResponseDraftResponse(UUID id, String content, DraftDecision decision, String provider, String model, Instant createdAt, Instant decidedAt) {
    public static ResponseDraftResponse from(ResponseDraft d) { return new ResponseDraftResponse(d.id(),d.content(),d.decision(),d.provider(),d.model(),d.createdAt(),d.decidedAt()); }
}
