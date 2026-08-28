package com.schwartzlizer.ai.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class ImageFeatureExtractorTest {
    @TempDir
    Path directory;

    @Test
    void extractsNormalizedAverageColor() throws IOException {
        Path image = writeSolidImage(directory.resolve("red.png"), Color.RED);

        ImageFeatures features = new ImageFeatureExtractor().extract(image);

        assertThat(features.width()).isEqualTo(2);
        assertThat(features.height()).isEqualTo(2);
        assertThat(features.red()).isCloseTo(1.0, offset(0.001));
        assertThat(features.green()).isZero();
        assertThat(features.blue()).isZero();
    }

    @Test
    void rejectsCorruptImage() throws IOException {
        Path image = directory.resolve("broken.png");
        Files.writeString(image, "not an image");

        assertThatThrownBy(() -> new ImageFeatureExtractor().extract(image))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported or corrupt image: broken.png");
    }

    private static Path writeSolidImage(Path path, Color color) throws IOException {
        var image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        ImageIO.write(image, "png", path.toFile());
        return path;
    }
}
