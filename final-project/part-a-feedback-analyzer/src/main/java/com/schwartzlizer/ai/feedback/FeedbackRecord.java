package com.schwartzlizer.ai.feedback;

public record FeedbackRecord(String reference, String message) {
    public FeedbackRecord {
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("reference is required");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("message is required");
        reference = reference.trim();
        message = message.trim();
    }
}
