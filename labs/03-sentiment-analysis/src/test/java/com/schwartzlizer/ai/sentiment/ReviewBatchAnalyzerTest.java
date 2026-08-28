package com.schwartzlizer.ai.sentiment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewBatchAnalyzerTest {
    @Test
    void preservesInputOrderAndReturnsImmutableResults() {
        var batch = new ReviewBatchAnalyzer(new LexiconSentimentAnalyzer());

        var results = batch.analyze(List.of("great service", "broken device", "Tuesday delivery"));

        assertThat(results).extracting(SentimentResult::sentiment)
                .containsExactly(Sentiment.POSITIVE, Sentiment.NEGATIVE, Sentiment.NEUTRAL);
        assertThatThrownBy(() -> results.add(new SentimentResult(Sentiment.NEUTRAL, 0)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
