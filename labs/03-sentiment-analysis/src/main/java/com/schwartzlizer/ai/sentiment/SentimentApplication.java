package com.schwartzlizer.ai.sentiment;

public final class SentimentApplication {
    private SentimentApplication() {
    }

    public static void main(String[] reviews) {
        var analyzer = new ReviewBatchAnalyzer(new LexiconSentimentAnalyzer());
        for (var result : analyzer.analyze(java.util.List.of(reviews))) {
            System.out.printf("%s score=%d%n", result.sentiment(), result.score());
        }
    }
}
