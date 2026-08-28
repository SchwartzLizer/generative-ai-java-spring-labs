package com.schwartzlizer.ai.image;

import java.util.Objects;

public record Prediction(String label, double confidence) {
    public Prediction {
        Objects.requireNonNull(label, "Label is required");
        if (label.isBlank() || !Double.isFinite(confidence) || confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Prediction confidence must be normalized to 0..1");
        }
    }
}
