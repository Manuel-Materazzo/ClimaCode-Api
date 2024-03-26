package com.manu.forecaster.utils;

import com.manu.forecaster.dto.TileBoundary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Extracts how many pixel in the source image within the search radius match the colors described in the legend
     * @param image source image for the match
     * @param legend map of names and hex colors to match
     * @param x x pixel of the search point
     * @param y y pixel of the search point
     * @param searchRadius pixels of search radius
     * @return a map containing the names of the legend, and the amount of matched pixels
     */
    public static Map<String, Integer> getColorMatchCount(BufferedImage image, Map<String, String> legend, int x, int y, int searchRadius) {

        // extract an RGB array
        int size = searchRadius * 2;
        int startx = x - searchRadius;
        int starty = y - searchRadius;
        int[] pixelColors = image.getRGB(startx, starty, size, size, null, 0, size);
        List<Integer> pixelColorsList = Arrays.stream(pixelColors).boxed().toList();

        Map<String, Integer> legendCounts = new HashMap<>();

        // iterate legend and count how many pixels there are with the same color as each legend entry
        for (var legendItem : legend.entrySet()) {
            // convert hex value to ARGB int
            int colorToSearch = hexToARGB(legendItem.getValue());
            // count the color matches into the pixel array
            int matches = Collections.frequency(pixelColorsList, colorToSearch);
            // save the counts for each legend entry on another map
            legendCounts.put(legendItem.getKey(), matches);
        }

        return legendCounts;
    }

    private static int hexToARGB(String hexColor) {

        // remove # to get only the hex
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        StringBuilder hexColorBuilder = new StringBuilder(hexColor);

        // pad the transparency if not provided
        while (hexColorBuilder.length() < 8) {
            hexColorBuilder.insert(0, "F");
        }

        hexColor = hexColorBuilder.toString();
        return (int) Long.parseLong(hexColor, 16);
    }

}
