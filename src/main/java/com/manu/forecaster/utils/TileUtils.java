package com.manu.forecaster.utils;

import com.manu.forecaster.dto.tile.TileRapresentation;

import java.math.BigDecimal;

public class TileUtils {

    private TileUtils(){}

    /**
     * Gets the WMS TileRapresentation of a given latitude and longitude with a certain zoom level
     *
     * @param latitude
     * @param longitude
     * @param zoom
     * @return
     */
    public static TileRapresentation latlongToTile(BigDecimal latitude, BigDecimal longitude, int zoom, int tileSize) {
        // scale = 2^zoom, using shift operator is more efficient
        int scale = 1 << zoom;
        double x = scale * (longitude.doubleValue() + 180) / 360;
        double y = scale * (1 - Math.log(Math.tan(latitude.doubleValue() * Math.PI / 180) + 1 / Math.cos(latitude.doubleValue() * Math.PI / 180)) / Math.PI) / 2;

        // Get the tile index
        int xtile = (int) x;
        int ytile = (int) y;

        // Get the pixel index within the tile
        int xpixel = (int) ((x - xtile) * tileSize);
        int ypixel = (int) ((y - ytile) * tileSize);

        return TileRapresentation.builder()
                .x(xtile)
                .y(ytile)
                .z(zoom)
                .xPixel(xpixel)
                .yPixel(ypixel)
                .build();

    }
}
