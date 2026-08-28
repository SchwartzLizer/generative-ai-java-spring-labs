package com.schwartzlizer.ai.image;

import java.util.Comparator;
import java.util.List;

public final class ProductImageClassifier {
    public List<Prediction> classify(ImageFeatures features) {
        if (features == null) {
            throw new IllegalArgumentException("Image features are required");
        }
        var predictions = new java.util.ArrayList<>(List.of(
                new Prediction("warm-colored product", clamp((features.red() - (features.green() + features.blue()) / 2 + 1) / 2)),
                new Prediction("cool-colored product", clamp((Math.max(features.green(), features.blue()) - features.red() + 1) / 2)),
                new Prediction("bright product", clamp(features.brightness())),
                new Prediction("dark product", clamp(1 - features.brightness()))));
        predictions.sort(Comparator.comparingDouble(Prediction::confidence).reversed()
                .thenComparing(Prediction::label));
        return List.copyOf(predictions);
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
