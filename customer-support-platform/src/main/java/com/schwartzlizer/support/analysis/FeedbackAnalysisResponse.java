package com.schwartzlizer.support.analysis;

import java.time.Instant;
import java.util.UUID;

public record FeedbackAnalysisResponse(UUID id, Sentiment sentiment, SupportCategory category, Urgency urgency, String recommendedAction, String provider, String model, Instant createdAt) {
    public static FeedbackAnalysisResponse from(FeedbackAnalysis a) { return new FeedbackAnalysisResponse(a.id(),a.sentiment(),a.category(),a.urgency(),a.recommendedAction(),a.provider(),a.model(),a.createdAt()); }
}
