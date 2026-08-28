package com.schwartzlizer.ai.recommendation;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Movie(String title, Set<String> genres, double rating) {
    public Movie {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title is required");
        }
        if (!Double.isFinite(rating) || rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Movie rating must be between 0 and 10");
        }
        Objects.requireNonNull(genres, "Movie genres are required");
        genres = Set.copyOf(genres.stream()
                .map(Objects::requireNonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet()));
    }
}
