package com.manu.forecaster.utils;

import com.manu.forecaster.dto.tile.TileBoundary;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    private static BufferedImage createSolidImage(int width, int height, int argb) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }

    // ── getColorMatchCount ──

    @Test
    void getColorMatchCount_fullCoverage() {
        // 10x10 image filled with opaque red (FFFF0000)
        int red = 0xFFFF0000;
        BufferedImage image = createSolidImage(10, 10, red);

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");

        Map<String, Integer> result = ImageUtils.getColorMatchCount(image, legend, 5, 5, 5);

        assertEquals(100, result.get("rain-1"));
    }

    @Test
    void getColorMatchCount_noMatchingColors() {
        // image filled with green, legend has red
        int green = 0xFF00FF00;
        BufferedImage image = createSolidImage(10, 10, green);

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");
        legend.put("#0000FF", "snow-1");

        Map<String, Integer> result = ImageUtils.getColorMatchCount(image, legend, 5, 5, 5);

        assertEquals(0, result.get("rain-1"));
        assertEquals(0, result.get("snow-1"));
    }

    @Test
    void getColorMatchCount_partialCoverage() {
        // 10x10 image: left half red, right half blue
        int red = 0xFFFF0000;
        int blue = 0xFF0000FF;
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                image.setRGB(x, y, x < 5 ? red : blue);
            }
        }

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");

        // center (5,5) radius 5 → startx=0, endx=10, starty=0, endy=10 → full image
        Map<String, Integer> result = ImageUtils.getColorMatchCount(image, legend, 5, 5, 5);

        assertEquals(50, result.get("rain-1"));
    }

    @Test
    void getColorMatchCount_clampedAtEdges() {
        // 10x10 solid red, search from corner (0,0) with large radius
        int red = 0xFFFF0000;
        BufferedImage image = createSolidImage(10, 10, red);

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");

        // searching from (0,0) with radius 20 → clamped to full image
        Map<String, Integer> result = ImageUtils.getColorMatchCount(image, legend, 0, 0, 20);

        assertEquals(100, result.get("rain-1"));
    }

    // ── getWeatherMatchByColor ──

    @Test
    void getWeatherMatchByColor_matchesLegendEntry() {
        int red = 0xFFFF0000;
        BufferedImage image = createSolidImage(5, 5, red);

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");
        legend.put("#0000FF", "snow-1");

        String result = ImageUtils.getWeatherMatchByColor(image, legend, 2, 2);

        assertEquals("rain-1", result);
    }

    @Test
    void getWeatherMatchByColor_noMatch_returnsClear() {
        int green = 0xFF00FF00;
        BufferedImage image = createSolidImage(5, 5, green);

        Map<String, String> legend = new HashMap<>();
        legend.put("#FF0000", "rain-1");
        legend.put("#0000FF", "snow-1");

        String result = ImageUtils.getWeatherMatchByColor(image, legend, 2, 2);

        assertEquals("clear", result);
    }

    // ── drawSquare ──

    @Test
    void drawSquare_returnsSameDimensions() {
        BufferedImage image = createSolidImage(50, 50, 0xFFFFFFFF);

        BufferedImage result = ImageUtils.drawSquare(image, 25, 25, 10);

        assertEquals(50, result.getWidth());
        assertEquals(50, result.getHeight());
    }

    // ── overlayImage ──

    @Test
    void overlayImage_sameDimensionsAndTypeArgb() {
        BufferedImage base = createSolidImage(20, 20, 0xFFFF0000);
        BufferedImage overlay = createSolidImage(20, 20, 0xFF0000FF);

        BufferedImage result = ImageUtils.overlayImage(base, overlay, 0.5f);

        assertEquals(20, result.getWidth());
        assertEquals(20, result.getHeight());
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType());
    }

    @Test
    void overlayImage_opacityZero_preservesBaseColors() {
        int red = 0xFFFF0000;
        BufferedImage base = createSolidImage(10, 10, red);
        BufferedImage overlay = createSolidImage(10, 10, 0xFF0000FF);

        BufferedImage result = ImageUtils.overlayImage(base, overlay, 0f);

        // with 0 opacity overlay is invisible, base colors should be preserved
        assertEquals(red, result.getRGB(5, 5));
    }

    // ── cropAndScale ──

    @Test
    void cropAndScale_correctDimensions() {
        BufferedImage source = createSolidImage(100, 100, 0xFFFF0000);
        TileBoundary boundary = TileBoundary.builder()
                .topLeftXPixel(10)
                .topLeftYPixel(10)
                .bottomRightXPixel(60)
                .bottomRightYPixel(60)
                .build();

        BufferedImage result = ImageUtils.cropAndScale(source, boundary, 2.0);

        // crop is 50x50, scaled by 2 → 100x100
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    // ── scale ──

    @Test
    void scale_doubleSize() {
        BufferedImage source = createSolidImage(100, 100, 0xFFFF0000);

        BufferedImage result = ImageUtils.scale(source, 2.0);

        assertEquals(200, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void scale_halfSize() {
        BufferedImage source = createSolidImage(100, 100, 0xFFFF0000);

        BufferedImage result = ImageUtils.scale(source, 0.5);

        assertEquals(50, result.getWidth());
        assertEquals(50, result.getHeight());
    }
}
