package com.schwartzlizer.ai.feedback;

import java.util.List;
import java.util.Map;

public record AnalysisSummary(List<FeedbackAnalysis> analyses, Map<Sentiment, Long> counts) {
    public AnalysisSummary {
        analyses = List.copyOf(analyses);
        counts = Map.copyOf(counts);
    }
}
