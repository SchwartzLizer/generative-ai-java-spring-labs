package com.schwartzlizer.ai.sentiment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LexiconSentimentAnalyzerTest {
    private final SentimentAnalyzer analyzer = new LexiconSentimentAnalyzer();

    @Test
    void classifiesPositiveTextIgnoringCaseAndPunctuation() {
        assertThat(analyzer.analyze("Excellent product, I LOVE it!").sentiment())
                .isEqualTo(Sentiment.POSITIVE);
    }

    @Test
    void classifiesNegativeText() {
        assertThat(analyzer.analyze("The device is broken and terrible").sentiment())
                .isEqualTo(Sentiment.NEGATIVE);
    }

    @Test
    void handlesImmediateNegation() {
        assertThat(analyzer.analyze("not good").sentiment())
                .isEqualTo(Sentiment.NEGATIVE);
    }

    @Test
    void returnsNeutralForNoKnownTerms() {
        assertThat(analyzer.analyze("The parcel arrived Tuesday").sentiment())
                .isEqualTo(Sentiment.NEUTRAL);
    }

    @Test
    void rejectsBlankReview() {
        assertThatThrownBy(() -> analyzer.analyze(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Review text is required");
    }
}
