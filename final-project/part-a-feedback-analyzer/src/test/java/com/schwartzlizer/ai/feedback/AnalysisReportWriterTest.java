package com.schwartzlizer.ai.feedback;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportWriterTest {
    @Test void writesStableHumanReadableReport() throws IOException {
        var summary = new AnalysisSummary(List.of(new FeedbackAnalysis("F-001", Sentiment.POSITIVE), new FeedbackAnalysis("F-002", Sentiment.NEGATIVE)), Map.of(Sentiment.POSITIVE, 1L, Sentiment.NEUTRAL, 0L, Sentiment.NEGATIVE, 1L));
        var output = new StringBuilder(); new AnalysisReportWriter().write(summary, output);
        assertThat(output.toString()).isEqualTo("Feedback Analysis\nF-001,POSITIVE\nF-002,NEGATIVE\nSummary: POSITIVE=1 NEUTRAL=0 NEGATIVE=1\n");
    }
}
