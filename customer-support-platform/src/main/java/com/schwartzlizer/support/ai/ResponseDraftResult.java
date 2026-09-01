package com.schwartzlizer.support.ai;

public record ResponseDraftResult(String content) {
    public ResponseDraftResult {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Draft content is required");
        content=content.trim();
    }
}
