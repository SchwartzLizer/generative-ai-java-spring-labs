package com.schwartzlizer.ai.recommendation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MovieRecommendationService {
    public List<Recommendation> recommend(List<Movie> catalog, UserPreference preference, int limit) {
        Objects.requireNonNull(catalog, "Movie catalog is required");
        Objects.requireNonNull(preference, "User preference is required");
        if (limit <= 0) {
            throw new IllegalArgumentException("Recommendation limit must be positive");
        }
        var recommendations = catalog.stream()
                .map(Objects::requireNonNull)
                .map(movie -> new Recommendation(movie, score(movie, preference)))
                .sorted(Comparator.comparingDouble(Recommendation::score).reversed()
                        .thenComparing(result -> result.movie().title()))
                .limit(limit)
                .toList();
        return List.copyOf(recommendations);
    }

    private static double score(Movie movie, UserPreference preference) {
        long matchingGenres = movie.genres().stream()
                .filter(preference.genres()::contains)
                .count();
        double score = 3.0 * matchingGenres + movie.rating() / 10.0;
        if (movie.rating() < preference.minimumRating()) {
            score -= 1.0;
        }
        return score;
    }
}
