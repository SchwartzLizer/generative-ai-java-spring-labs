package com.schwartzlizer.support.dashboard;

import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.feedback.FeedbackStatus;
import java.util.Map;

public record DashboardSummary(long total, long open, long urgent, long pendingDrafts, Map<Sentiment,Long> sentimentCounts, Map<SupportCategory,Long> categoryCounts, Map<Urgency,Long> urgencyCounts, Map<FeedbackStatus,Long> statusCounts) {
    public DashboardSummary { sentimentCounts=Map.copyOf(sentimentCounts); categoryCounts=Map.copyOf(categoryCounts); urgencyCounts=Map.copyOf(urgencyCounts); statusCounts=Map.copyOf(statusCounts); }
}
