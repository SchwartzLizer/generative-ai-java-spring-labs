package com.schwartzlizer.ai.feedback;

import java.io.IOException;

public final class AnalysisReportWriter {
    public void write(AnalysisSummary summary, Appendable output) throws IOException {
        output.append("Feedback Analysis\n");
        for (FeedbackAnalysis analysis : summary.analyses()) output.append(analysis.reference()).append(',').append(analysis.sentiment().name()).append('\n');
        output.append("Summary: POSITIVE=").append(summary.counts().getOrDefault(Sentiment.POSITIVE, 0L).toString())
            .append(" NEUTRAL=").append(summary.counts().getOrDefault(Sentiment.NEUTRAL, 0L).toString())
            .append(" NEGATIVE=").append(summary.counts().getOrDefault(Sentiment.NEGATIVE, 0L).toString()).append('\n');
    }
}
