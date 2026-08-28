package com.schwartzlizer.ai.sentiment;

import java.util.List;
import java.util.Objects;

public final class ReviewBatchAnalyzer {
    private final SentimentAnalyzer analyzer;

    public ReviewBatchAnalyzer(SentimentAnalyzer analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer, "Analyzer is required");
    }

    public List<SentimentResult> analyze(List<String> reviews) {
        Objects.requireNonNull(reviews, "Reviews are required");
        return List.copyOf(reviews.stream().map(analyzer::analyze).toList());
    }
}
