package com.manu.forecaster.utils;

import com.manu.forecaster.dto.tile.Coordinate;
import com.manu.forecaster.dto.tile.TileRapresentation;

import java.math.BigDecimal;

public class TileUtils {

    private TileUtils() {
    }

    /**
     * Gets the WMS TileRapresentation of a given latitude and longitude with a certain zoom level
     *
     * @param latitude latitude of the point within the tile to extract
     * @param longitude longitude of the point within the tile to extract
     * @param zoom zoom level (z) of the tile to extract
     * @return a complete TileRapresentation, containing all the tile data and the pixel index of the requested point
     */
    public static TileRapresentation latlongToTile(BigDecimal latitude, BigDecimal longitude, int zoom, int tileSize) {
        // scale = 2^zoom, using shift operator is more efficient
        int scale = 1 << zoom;
        double x = scale * (longitude.doubleValue() + 180) / 360;
        double y = scale * (1 - Math.log(Math.tan(latitude.doubleValue() * Math.PI / 180) + 1 / Math.cos(latitude.doubleValue() * Math.PI / 180)) / Math.PI) / 2;

        // Get the tile index
        int xtile = (int) x;
        int ytile = (int) y;

        // Get the partial tile rapresentation with corners
        TileRapresentation corners = getTileCornersCoordinates(xtile, ytile, zoom);

        // Get the pixel index within the tile
        int xpixel = (int) ((x - xtile) * tileSize);
        int ypixel = (int) ((y - ytile) * tileSize);

        return TileRapresentation.builder()
                .x(xtile)
                .y(ytile)
                .z(zoom)
                .topLeftCorner(corners.getTopLeftCorner())
                .bottomRightCorner(corners.getBottomRightCorner())
                .xPixel(xpixel)
                .yPixel(ypixel)
                .build();

    }

    /**
     * Computes a partial TileRapresentation containing only the corners coordinate of the given xyz tile
     * @param x x of the tile
     * @param y y of the tile
     * @param z z of the tile
     * @return a partial TileRapresentation with only corners coordinates
     */
    private static TileRapresentation getTileCornersCoordinates(int x, int y, int z) {
        // Total number of tiles
        double n = (int) Math.pow(2, z);

        // Calculate coordinates of the top-left corner
        BigDecimal lat1 = BigDecimal.valueOf(Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n)))));
        BigDecimal lon1 = BigDecimal.valueOf(x / n * 360.0 - 180);


        // Calculate latitude and longitude of the bottom-right corner
        BigDecimal lat2 = BigDecimal.valueOf(Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n)))));
        BigDecimal lon2 = BigDecimal.valueOf((x + 1) / n * 360.0 - 180);


        // Create coordinates Objects for each corner
        Coordinate topLeftCorner = Coordinate.builder()
                .latitude(lat1)
                .longitude(lon1)
                .build();

        Coordinate bottomRightCorner = Coordinate.builder()
                .latitude(lat2)
                .longitude(lon2)
                .build();

        return TileRapresentation.builder()
                .topLeftCorner(topLeftCorner)
                .bottomRightCorner(bottomRightCorner)
                .build();
    }
}
