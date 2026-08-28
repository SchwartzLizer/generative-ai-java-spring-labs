package com.schwartzlizer.ai.sentiment;

import java.util.Objects;

public record SentimentResult(Sentiment sentiment, int score) {
    public SentimentResult {
        Objects.requireNonNull(sentiment, "Sentiment is required");
    }
}
