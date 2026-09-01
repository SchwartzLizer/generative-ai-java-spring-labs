package com.schwartzlizer.support.analysis;

public record AnalysisInput(String message) {
    public AnalysisInput {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Feedback message is required");
        }
    }
}
