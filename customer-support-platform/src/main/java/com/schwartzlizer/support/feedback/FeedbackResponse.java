package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.analysis.FeedbackAnalysisResponse;
import com.schwartzlizer.support.response.ResponseDraftResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedbackResponse(UUID id, String customerReference, String message, FeedbackStatus status, Instant createdAt, Instant updatedAt, List<FeedbackAnalysisResponse> analyses, List<ResponseDraftResponse> drafts) {
    public FeedbackResponse {
        analyses=List.copyOf(analyses == null ? List.of() : analyses);
        drafts=List.copyOf(drafts == null ? List.of() : drafts);
    }
    public static FeedbackResponse basic(Feedback f) { return new FeedbackResponse(f.id(), f.customerReference(), f.message(), f.status(), f.createdAt(), f.updatedAt(), List.of(), List.of()); }
}
