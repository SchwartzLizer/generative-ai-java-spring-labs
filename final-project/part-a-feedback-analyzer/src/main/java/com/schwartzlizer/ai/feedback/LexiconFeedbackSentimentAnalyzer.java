package com.schwartzlizer.ai.feedback;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LexiconFeedbackSentimentAnalyzer implements FeedbackSentimentAnalyzer {
    private static final Set<String> POSITIVE = Set.of("fast", "helpful", "excellent", "resolved", "easy", "satisfied");
    private static final Set<String> NEGATIVE = Set.of("broken", "late", "difficult", "unhelpful", "error", "frustrated");
    private static final Set<String> NEGATORS = Set.of("not", "never", "no");

    @Override
    public Sentiment analyze(String message) {
        if (message == null || message.isBlank()) return Sentiment.NEUTRAL;
        int score = 0;
        String previous = "";
        for (String token : message.toLowerCase(Locale.ROOT).split("[^a-z]+")) {
            if (token.isBlank()) continue;
            int weight = POSITIVE.contains(token) ? 1 : NEGATIVE.contains(token) ? -1 : 0;
            if (weight != 0) { score += NEGATORS.contains(previous) ? -weight : weight; }
            previous = token;
        }
        return score > 0 ? Sentiment.POSITIVE : score < 0 ? Sentiment.NEGATIVE : Sentiment.NEUTRAL;
    }
}
