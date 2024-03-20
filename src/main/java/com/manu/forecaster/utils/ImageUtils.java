package com.manu.forecaster.utils;

import java.awt.image.BufferedImage;
import java.util.*;

public class ImageUtils {

    private ImageUtils() {
    }

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
