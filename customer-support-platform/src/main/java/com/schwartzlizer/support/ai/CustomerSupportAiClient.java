package com.schwartzlizer.support.ai;

public interface CustomerSupportAiClient {
    FeedbackAnalysisResult analyze(String feedbackMessage);
    ResponseDraftResult draftResponse(String feedbackMessage, FeedbackAnalysisResult analysis);
}
