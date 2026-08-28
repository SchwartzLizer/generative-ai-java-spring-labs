package com.schwartzlizer.ai.image;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageFeatureExtractor {
    public ImageFeatures extract(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            throw unsupported(path);
        }
        try {
            var image = ImageIO.read(path.toFile());
            if (image == null) {
                throw unsupported(path);
            }
            long red = 0;
            long green = 0;
            long blue = 0;
            long pixels = (long) image.getWidth() * image.getHeight();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    red += (rgb >> 16) & 0xff;
                    green += (rgb >> 8) & 0xff;
                    blue += rgb & 0xff;
                }
            }
            double averageRed = red / (pixels * 255.0);
            double averageGreen = green / (pixels * 255.0);
            double averageBlue = blue / (pixels * 255.0);
            return new ImageFeatures(image.getWidth(), image.getHeight(), averageRed, averageGreen,
                    averageBlue, (averageRed + averageGreen + averageBlue) / 3.0);
        } catch (IOException exception) {
            throw unsupported(path);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw unsupported(path);
        }
    }

    private static IllegalArgumentException unsupported(Path path) {
        var filename = path == null || path.getFileName() == null ? "<unknown>" : path.getFileName().toString();
        return new IllegalArgumentException("Unsupported or corrupt image: " + filename);
    }
}
