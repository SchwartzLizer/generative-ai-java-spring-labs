package com.schwartzlizer.ai.feedback;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public final class FeedbackAnalysisService {
    private final FeedbackSentimentAnalyzer analyzer;
    public FeedbackAnalysisService(FeedbackSentimentAnalyzer analyzer) { this.analyzer = Objects.requireNonNull(analyzer); }
    public AnalysisSummary analyze(List<FeedbackRecord> records) {
        Objects.requireNonNull(records);
        EnumMap<Sentiment, Long> counts = new EnumMap<>(Sentiment.class);
        for (Sentiment sentiment : Sentiment.values()) counts.put(sentiment, 0L);
        List<FeedbackAnalysis> analyses = records.stream().map(record -> {
            Sentiment sentiment = Objects.requireNonNull(analyzer.analyze(record.message()), "analyzer returned null");
            counts.compute(sentiment, (key, value) -> value + 1);
            return new FeedbackAnalysis(record.reference(), sentiment);
        }).toList();
        return new AnalysisSummary(analyses, counts);
    }
}
