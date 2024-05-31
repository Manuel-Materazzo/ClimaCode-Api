package com.manu.forecaster.utils;

import com.manu.forecaster.dto.tile.TileBoundary;

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
     *
     * @param image        source image for the match
     * @param legend       map of names and hex colors to match
     * @param x            x pixel of the search point
     * @param y            y pixel of the search point
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
            int colorToSearch = hexToARGB(legendItem.getKey());
            // count the color matches into the pixel array
            int matches = Collections.frequency(pixelColorsList, colorToSearch);
            // save the counts for each legend entry on another map
            legendCounts.put(legendItem.getValue(), matches);
        }

        return legendCounts;
    }


    public static String getWeatherMatchByColor(BufferedImage image, Map<String, String> legend, int x, int y) {

        int pixelColor = image.getRGB(x, y);

        // iterate legend to search the corresponding pixel color
        for (var legendItem : legend.entrySet()) {
            // convert hex value to ARGB int
            int colorToSearch = hexToARGB(legendItem.getKey());
            // if the color matches, return it
            if (colorToSearch == pixelColor) {
                return legendItem.getValue();
            }
        }

        // if nothing matches, the weather is clear
        return "clear";
    }

    public static BufferedImage drawSquare(BufferedImage image, int centroidX, int centroidY, int radius) {

        int size = radius * 2 + 1;
        int startx = centroidX - radius;
        int starty = centroidY - radius;

        Graphics2D graph = image.createGraphics();
        graph.setColor(Color.BLACK);
        graph.drawRect(startx, starty, size, size);
        graph.dispose();

        return image;
    }

    /**
     * Sets the opacity of the overlayImage to the value provided and draws it on top of the baseImage
     *
     * @param baseImage    image at full opacity
     * @param overlayImage image with reduced opacity to overlay
     * @param opacity      opacity of overlayImage
     * @return BufferedImage of overlapped images
     */
    public static BufferedImage overlayImage(BufferedImage baseImage, BufferedImage overlayImage, float opacity) {

        // create a new ARGB image, if skipped a jpg baseimage can mess with color channels
        BufferedImage newBaseImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        newBaseImage.getGraphics().drawImage(baseImage, 0, 0, null);

        // create a 2D graphics canvas
        Graphics2D g = newBaseImage.createGraphics();

        // set the alpha composition to "overlay" the second image
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g.setComposite(ac);

        // draw the overlay
        g.drawImage(overlayImage, 0, 0, null);

        g.dispose();
        return newBaseImage;
    }

    /**
     * Crops the source image to the provided boundary and scales is
     *
     * @param source   image to crop
     * @param boundary boundaries for crop
     * @param scale    scale of the output image
     * @return the source image, cropped and scaled
     */
    public static BufferedImage cropAndScale(BufferedImage source, TileBoundary boundary, double scale) {

        int width = boundary.getBottomRightXPixel() - boundary.getTopLeftXPixel();
        int height = boundary.getBottomRightYPixel() - boundary.getTopLeftYPixel();

        // calculate the resampled image size
        int scaledWidth = (int) (width * scale);
        int scaledHeigth = (int) (height * scale);

        // crop the image
        BufferedImage cropped = source.getSubimage(boundary.getTopLeftXPixel(), boundary.getTopLeftYPixel(), width, height);

        // scale the image by resampling with bilinear filter
        BufferedImage after = new BufferedImage(scaledWidth, scaledHeigth, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(cropped, after);
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
