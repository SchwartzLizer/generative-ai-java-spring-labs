package com.schwartzlizer.ai.feedback;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class FeedbackAnalysisServiceTest {
    @Test void returnsPerRecordAnalysisAndCompleteCounts() {
        FeedbackSentimentAnalyzer analyzer = message -> message.contains("fast") ? Sentiment.POSITIVE : message.contains("broken") ? Sentiment.NEGATIVE : Sentiment.NEUTRAL;
        AnalysisSummary result = new FeedbackAnalysisService(analyzer).analyze(List.of(new FeedbackRecord("F-001", "Delivery was fast"), new FeedbackRecord("F-002", "The item arrived broken"), new FeedbackRecord("F-003", "Package arrived Tuesday")));
        assertThat(result.analyses()).containsExactly(new FeedbackAnalysis("F-001", Sentiment.POSITIVE), new FeedbackAnalysis("F-002", Sentiment.NEGATIVE), new FeedbackAnalysis("F-003", Sentiment.NEUTRAL));
        assertThat(result.counts()).containsEntry(Sentiment.POSITIVE, 1L).containsEntry(Sentiment.NEGATIVE, 1L).containsEntry(Sentiment.NEUTRAL, 1L);
    }
    @Test void includesZeroCountsForEmptyInput() { assertThat(new FeedbackAnalysisService(message -> Sentiment.NEUTRAL).analyze(List.of()).counts()).containsOnly(entry(Sentiment.POSITIVE, 0L), entry(Sentiment.NEUTRAL, 0L), entry(Sentiment.NEGATIVE, 0L)); }
}
