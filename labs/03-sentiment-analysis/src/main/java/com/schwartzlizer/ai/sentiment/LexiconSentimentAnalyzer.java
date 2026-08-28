package com.schwartzlizer.ai.sentiment;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LexiconSentimentAnalyzer implements SentimentAnalyzer {
    private static final Set<String> NEGATORS = Set.of("not", "never", "no");
    private static final Map<String, Integer> LEXICON = Map.ofEntries(
            Map.entry("good", 1), Map.entry("great", 1), Map.entry("excellent", 1),
            Map.entry("love", 1), Map.entry("helpful", 1), Map.entry("fast", 1),
            Map.entry("bad", -1), Map.entry("terrible", -1), Map.entry("broken", -1),
            Map.entry("hate", -1), Map.entry("slow", -1), Map.entry("poor", -1));

    @Override
    public SentimentResult analyze(String review) {
        if (review == null || review.isBlank()) {
            throw new IllegalArgumentException("Review text is required");
        }
        int score = 0;
        boolean negated = false;
        for (String token : review.toLowerCase(Locale.ROOT).split("[^a-z']+")) {
            if (token.isEmpty()) {
                continue;
            }
            if (NEGATORS.contains(token)) {
                negated = true;
                continue;
            }
            Integer tokenScore = LEXICON.get(token);
            if (tokenScore == null) {
                negated = false;
                continue;
            }
            score += negated ? -tokenScore : tokenScore;
            negated = false;
        }
        return new SentimentResult(classify(score), score);
    }

    private static Sentiment classify(int score) {
        return score > 0 ? Sentiment.POSITIVE : score < 0 ? Sentiment.NEGATIVE : Sentiment.NEUTRAL;
    }
}
