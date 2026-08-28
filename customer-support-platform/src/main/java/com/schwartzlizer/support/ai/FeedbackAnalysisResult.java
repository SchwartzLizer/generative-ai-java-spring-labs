package com.schwartzlizer.support.ai;

import com.schwartzlizer.support.analysis.*;

public record FeedbackAnalysisResult(Sentiment sentiment, SupportCategory category, Urgency urgency, String recommendedAction) {
    public FeedbackAnalysisResult { if (sentiment == null || category == null || urgency == null || recommendedAction == null || recommendedAction.isBlank()) throw new IllegalArgumentException("Analysis result fields are required"); recommendedAction=recommendedAction.trim(); }
}
