package com.schwartzlizer.ai.image;

public record ImageFeatures(int width, int height, double red, double green, double blue, double brightness) {
    public ImageFeatures {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
        requireNormalized(red, "red");
        requireNormalized(green, "green");
        requireNormalized(blue, "blue");
        requireNormalized(brightness, "brightness");
    }

    private static void requireNormalized(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException(name + " must be normalized to 0..1");
        }
    }
}
