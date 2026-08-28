package com.schwartzlizer.ai.image;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageClassifierTest {
    @Test
    void ranksWarmProductForRedDominantImage() {
        var predictions = new ProductImageClassifier().classify(
                new ImageFeatures(100, 100, 0.9, 0.2, 0.1, 0.4));

        assertThat(predictions).first()
                .extracting(Prediction::label)
                .isEqualTo("warm-colored product");
        assertThat(predictions).isSortedAccordingTo(
                Comparator.comparingDouble(Prediction::confidence).reversed());
    }
}
