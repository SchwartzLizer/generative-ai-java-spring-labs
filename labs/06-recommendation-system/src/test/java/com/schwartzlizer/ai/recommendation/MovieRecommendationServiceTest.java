package com.schwartzlizer.ai.recommendation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MovieRecommendationServiceTest {
    private final MovieRecommendationService service = new MovieRecommendationService();

    @Test
    void ranksGenreMatchesBeforeRatingOnlyMatches() {
        var catalog = List.of(
                new Movie("Space Journey", Set.of("sci-fi"), 8.0),
                new Movie("Award Drama", Set.of("drama"), 9.8));

        assertThat(service.recommend(
                catalog, new UserPreference(Set.of("sci-fi"), 7.0), 2))
                .extracting(result -> result.movie().title())
                .containsExactly("Space Journey", "Award Drama");
    }

    @Test
    void breaksEqualScoresByTitle() {
        var catalog = List.of(
                new Movie("Zulu", Set.of("drama"), 8.0),
                new Movie("Alpha", Set.of("drama"), 8.0));

        assertThat(service.recommend(
                catalog, new UserPreference(Set.of("drama"), 0), 2))
                .extracting(result -> result.movie().title())
                .containsExactly("Alpha", "Zulu");
    }
}
