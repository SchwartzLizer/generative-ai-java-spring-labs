package com.schwartzlizer.ai.recommendation;

import java.util.List;
import java.util.Set;

public final class RecommendationApplication {
    private RecommendationApplication() {
    }

    public static void main(String[] args) {
        var catalog = List.of(
                new Movie("Space Journey", Set.of("sci-fi", "adventure"), 8.5),
                new Movie("Award Drama", Set.of("drama"), 9.8),
                new Movie("Ocean Mystery", Set.of("adventure"), 7.5));
        var preference = new UserPreference(Set.of("sci-fi", "adventure"), 7.0);
        new MovieRecommendationService().recommend(catalog, preference, 3)
                .forEach(result -> System.out.printf("%s score=%.2f%n", result.movie().title(), result.score()));
    }
}
