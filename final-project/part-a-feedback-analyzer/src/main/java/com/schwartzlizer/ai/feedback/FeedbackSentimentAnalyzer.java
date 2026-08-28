package com.schwartzlizer.ai.feedback;

@FunctionalInterface
public interface FeedbackSentimentAnalyzer { Sentiment analyze(String message); }
