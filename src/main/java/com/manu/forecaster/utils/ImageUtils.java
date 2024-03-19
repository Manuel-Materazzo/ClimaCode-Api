package com.manu.forecaster.utils;

import java.awt.image.BufferedImage;
import java.util.*;

public class ImageUtils {

    public Map<String, Integer> getColorMatchCount(BufferedImage image, Map<String, String> legend, int x, int y, int searchRadius) {

        // extract an RGB array
        int size = searchRadius * 2;
        int startx = x - searchRadius;
        int starty = y - searchRadius;
        int[] pixelColors = image.getRGB(startx, starty, size, size, null, 0, image.getWidth());
        List<Integer> pixelColorsList = Arrays.stream(pixelColors).boxed().toList();

        Map<String, Integer> legendCounts = new HashMap<>();

        // iterate legend and count how many pixels there are with the same color as each legend entry
        for (var legendItem : legend.entrySet()) {
            // convert hex value to ARGB int
            String hexColor = legendItem.getValue();
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            int colorToSearch = (int) Long.parseLong(hexColor, 16);
            // count the color matches into the pixel array
            int matches = Collections.frequency(pixelColorsList, colorToSearch);
            // save the counts for each legend entry on another map
            legendCounts.put(legendItem.getKey(), matches);
        }

        return legendCounts;
    }

}
