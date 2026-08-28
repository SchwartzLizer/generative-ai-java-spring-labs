package com.schwartzlizer.ai.image;

import java.nio.file.Path;
import java.util.Locale;

public final class ImageRecognitionApplication {
    private ImageRecognitionApplication() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar image-recognition-*.jar <image-path>");
            System.exit(2);
        }
        try {
            var features = new ImageFeatureExtractor().extract(Path.of(args[0]));
            for (var prediction : new ProductImageClassifier().classify(features)) {
                System.out.printf(Locale.ROOT, "%s confidence=%.3f%n", prediction.label(), prediction.confidence());
            }
        } catch (RuntimeException exception) {
            System.err.println("Image recognition failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}
