package com.schwartzlizer.ai.recommendation;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record UserPreference(Set<String> genres, double minimumRating) {
    public UserPreference {
        Objects.requireNonNull(genres, "Preference genres are required");
        if (!Double.isFinite(minimumRating) || minimumRating < 0 || minimumRating > 10) {
            throw new IllegalArgumentException("Minimum rating must be between 0 and 10");
        }
        genres = Set.copyOf(genres.stream()
                .map(Objects::requireNonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet()));
    }
}
