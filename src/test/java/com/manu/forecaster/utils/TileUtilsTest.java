package com.manu.forecaster.utils;

import com.manu.forecaster.dto.tile.TileBoundary;
import com.manu.forecaster.dto.tile.TileRapresentation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TileUtilsTest {

    // ───────────────────────── latlongToTile tests ─────────────────────────

    @Test
    void latlongToTile_rome_zoom6_512() {
        TileRapresentation tile = TileUtils.latlongToTile(
                new BigDecimal("41.9028"), new BigDecimal("12.4964"), 6, 512);

        assertEquals(34, tile.getX());
        assertEquals(23, tile.getY());
        assertEquals(6, tile.getZ());
        assertEquals(113, tile.getXPixel());
        assertEquals(399, tile.getYPixel());
    }

    @Test
    void latlongToTile_equatorPrimeMeridian_zoom1() {
        TileRapresentation tile = TileUtils.latlongToTile(
                BigDecimal.ZERO, BigDecimal.ZERO, 1, 256);

        assertEquals(1, tile.getX());
        assertEquals(1, tile.getY());
        assertEquals(1, tile.getZ());
        assertEquals(0, tile.getXPixel());
        assertEquals(0, tile.getYPixel());
    }

    @Test
    void latlongToTile_equatorPrimeMeridian_zoom4() {
        TileRapresentation tile = TileUtils.latlongToTile(
                BigDecimal.ZERO, BigDecimal.ZERO, 4, 256);

        assertEquals(8, tile.getX());
        assertEquals(8, tile.getY());
        assertEquals(4, tile.getZ());
        assertEquals(0, tile.getXPixel());
        assertEquals(0, tile.getYPixel());
    }

    @Test
    void latlongToTile_negativeCoordinates_brasilia() {
        TileRapresentation tile = TileUtils.latlongToTile(
                new BigDecimal("-15.7797"), new BigDecimal("-47.9297"), 8, 256);

        assertEquals(93, tile.getX());
        assertEquals(139, tile.getY());
        assertEquals(8, tile.getZ());
        assertEquals(234, tile.getXPixel());
        assertEquals(93, tile.getYPixel());
    }

    @Test
    void latlongToTile_highZoom_rome() {
        TileRapresentation tile = TileUtils.latlongToTile(
                new BigDecimal("41.9028"), new BigDecimal("12.4964"), 18, 256);

        assertEquals(140171, tile.getX());
        assertEquals(97407, tile.getY());
        assertEquals(18, tile.getZ());
        assertEquals(153, tile.getXPixel());
        assertEquals(125, tile.getYPixel());
    }

    @Test
    void latlongToTile_cornerCoordinatesAreComputed() {
        TileRapresentation tile = TileUtils.latlongToTile(
                new BigDecimal("41.9028"), new BigDecimal("12.4964"), 6, 512);

        assertNotNull(tile.getTopLeftCorner());
        assertNotNull(tile.getBottomRightCorner());
        assertNotNull(tile.getTopLeftCorner().getLatitude());
        assertNotNull(tile.getTopLeftCorner().getLongitude());
        assertNotNull(tile.getBottomRightCorner().getLatitude());
        assertNotNull(tile.getBottomRightCorner().getLongitude());
    }

    // ───────────────────────── getBoundaries tests ─────────────────────────

    @Test
    void getBoundaries_sameTile_pixelsPassThrough() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(5).y(5).xPixel(50).yPixel(30).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(5).y(5).xPixel(200).yPixel(180).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(50, boundary.getTopLeftXPixel());
        assertEquals(30, boundary.getTopLeftYPixel());
        assertEquals(200, boundary.getBottomRightXPixel());
        assertEquals(180, boundary.getBottomRightYPixel());
    }

    @Test
    void getBoundaries_topLeftSmallerX_correctedToZero() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(4).y(5).xPixel(100).yPixel(30).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(5).y(5).xPixel(200).yPixel(180).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(0, boundary.getTopLeftXPixel());
        assertEquals(30, boundary.getTopLeftYPixel());
        assertEquals(200, boundary.getBottomRightXPixel());
        assertEquals(180, boundary.getBottomRightYPixel());
    }

    @Test
    void getBoundaries_bottomRightLargerX_correctedToWidth() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(5).y(5).xPixel(50).yPixel(30).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(6).y(5).xPixel(200).yPixel(180).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(50, boundary.getTopLeftXPixel());
        assertEquals(30, boundary.getTopLeftYPixel());
        assertEquals(512, boundary.getBottomRightXPixel());
        assertEquals(180, boundary.getBottomRightYPixel());
    }

    @Test
    void getBoundaries_topLeftSmallerY_correctedToZero() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(5).y(4).xPixel(50).yPixel(100).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(5).y(5).xPixel(200).yPixel(180).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(50, boundary.getTopLeftXPixel());
        assertEquals(0, boundary.getTopLeftYPixel());
        assertEquals(200, boundary.getBottomRightXPixel());
        assertEquals(180, boundary.getBottomRightYPixel());
    }

    @Test
    void getBoundaries_bottomRightLargerY_correctedToHeight() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(5).y(5).xPixel(50).yPixel(30).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(5).y(6).xPixel(200).yPixel(180).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(50, boundary.getTopLeftXPixel());
        assertEquals(30, boundary.getTopLeftYPixel());
        assertEquals(200, boundary.getBottomRightXPixel());
        assertEquals(512, boundary.getBottomRightYPixel());
    }

    @Test
    void getBoundaries_allBordersTouched_allPixelsCorrected() {
        TileRapresentation original = TileRapresentation.builder().x(5).y(5).build();
        TileRapresentation topLeft = TileRapresentation.builder().x(4).y(4).xPixel(100).yPixel(100).build();
        TileRapresentation bottomRight = TileRapresentation.builder().x(6).y(6).xPixel(200).yPixel(200).build();

        TileBoundary boundary = TileUtils.getBoundaries(512, 512, original, topLeft, bottomRight);

        assertEquals(0, boundary.getTopLeftXPixel());
        assertEquals(0, boundary.getTopLeftYPixel());
        assertEquals(512, boundary.getBottomRightXPixel());
        assertEquals(512, boundary.getBottomRightYPixel());
    }
}
