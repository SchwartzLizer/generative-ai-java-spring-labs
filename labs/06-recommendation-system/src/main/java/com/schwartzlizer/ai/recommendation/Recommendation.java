package com.schwartzlizer.ai.recommendation;

public record Recommendation(Movie movie, double score) {
    public Recommendation {
        if (movie == null || !Double.isFinite(score)) {
            throw new IllegalArgumentException("Recommendation is invalid");
        }
    }
}
