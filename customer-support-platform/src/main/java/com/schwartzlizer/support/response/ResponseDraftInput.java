package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.FeedbackAnalysisResult;

public record ResponseDraftInput(String message, FeedbackAnalysisResult analysis) {
    public ResponseDraftInput {
        if (message == null || message.isBlank() || analysis == null) {
            throw new IllegalArgumentException("Response draft input is required");
        }
    }
}
