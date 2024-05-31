package com.manu.forecaster.service;

import com.manu.forecaster.dto.configuration.TileRadarImageryConfig;
import com.manu.forecaster.dto.nowcast.Nowcast;
import com.manu.forecaster.dto.tile.TileBoundary;
import com.manu.forecaster.dto.tile.TileRapresentation;
import com.manu.forecaster.dto.configuration.TileRadarConfig;
import com.manu.forecaster.exception.ConfigurationException;
import com.manu.forecaster.exception.RestException;
import com.manu.forecaster.utils.ImageUtils;
import com.manu.forecaster.utils.TileUtils;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class TileRadarService {

    private final TileRadarConfig tileRadarConfig;
    private final RestService restService;
    private final SpelService spelService;
    private final String baseMapUrl;
    private final int baseMapZoomLevel;

    public TileRadarService(TileRadarConfig tileRadarConfig, RestService restService, SpelService spelService,
                            int baseMapZoomLevel, String baseMapUrl) {
        this.tileRadarConfig = tileRadarConfig;
        this.restService = restService;
        this.spelService = spelService;
        this.baseMapUrl = baseMapUrl;
        this.baseMapZoomLevel = baseMapZoomLevel;
    }

    public String getName() {
        return tileRadarConfig.getName();
    }

    public List<Nowcast> getNowcasts(BigDecimal latitude, BigDecimal longitude) throws IOException, RestException {

        List<Nowcast> nowcasts = new ArrayList<>();

        for (var imagery : tileRadarConfig.getImagery()) {
            // calculate Tile and pixel position within tile
            TileRapresentation tile = TileUtils.latlongToTile(latitude, longitude, tileRadarConfig.getZoomLevel(), 512);

            // get weather radar tile image
            BufferedImage weatherRadarTileImage = getImage(
                    imagery.getUrl(), imagery.getMethod(), tileRadarConfig.getHeaders(), imagery.getBody(), imagery.getBodyContentType(), tile
            );

            // get how many pixels around the point of interest have matching colors with the legend
            // pro tip: using a 5px search radius, we search on 100px, so you can read the pixel number as a coverage %
            Map<String, Integer> forecast = ImageUtils.getColorMatchCount(
                    weatherRadarTileImage, tileRadarConfig.getLegend(), tile.getXPixel(), tile.getYPixel(), 5
            );

            String pointWeather = ImageUtils.getWeatherMatchByColor(weatherRadarTileImage, tileRadarConfig.getLegend(), tile.getXPixel(), tile.getYPixel());

            // add to the forecasts list
            nowcasts.add(
                    Nowcast.builder()
                            .imageryName(imagery.getName())
                            .pointWeatherCondition(pointWeather)
                            .areaWeatherCoinditions(forecast)
                            .build()
            );
        }

        return nowcasts;
    }

    /**
     * Gets the highlighted nowcast image from the radar
     *
     * @param latitude
     * @param longitude
     * @param name      name of the imagery of the radar
     * @return a bufferedImage containing radar image overlayed on the base map
     * @throws IOException
     * @throws RestException
     */
    public BufferedImage getNowcastImage(BigDecimal latitude, BigDecimal longitude, String name) throws IOException, RestException {

        // the radar can't be more accurate than the base map.
        if (tileRadarConfig.getZoomLevel() > baseMapZoomLevel) {
            throw new ConfigurationException("The tile radar zoom level is greater than the base map zoom level");
        }

        // search the correct imagery config
        Optional<TileRadarImageryConfig> optionalImagery = tileRadarConfig.getImagery().stream().filter(service -> name.contains(service.getName())).findFirst();
        TileRadarImageryConfig imagery = optionalImagery.orElseThrow();

        // calculate Tiles and pixel position within tile
        TileRapresentation weatherRadarTile = TileUtils.latlongToTile(latitude, longitude, tileRadarConfig.getZoomLevel(), 512);
        TileRapresentation baseMapTile = TileUtils.latlongToTile(latitude, longitude, baseMapZoomLevel, 512);

        // get weather radar tile image
        BufferedImage weatherRadarTileImage = getImage(
                imagery.getUrl(), imagery.getMethod(), tileRadarConfig.getHeaders(), imagery.getBody(), imagery.getBodyContentType(), weatherRadarTile
        );

        // if the zoom level is not the same
        if (baseMapZoomLevel != tileRadarConfig.getZoomLevel()) {
            // crop the weather radar image to extract only the portion covered by the base map tile
            // and scale it back to the original size
            weatherRadarTileImage = zoomTileImagery(weatherRadarTileImage, weatherRadarTile, baseMapTile, 512);
        }

        // get base map tile image
        BufferedImage baseMapImage = getImage(
                baseMapUrl, "GET", new HashMap<>(), null, null, baseMapTile
        );

        // overlay the weather radar image on top of the base map
        BufferedImage overlayedImage = ImageUtils.overlayImage(baseMapImage, weatherRadarTileImage, tileRadarConfig.getOpacity());

        // scale difference between the base map and the tile radar
        int scale = (int) Math.pow(2, (double) baseMapZoomLevel - tileRadarConfig.getZoomLevel());

        return ImageUtils.drawSquare(overlayedImage, baseMapTile.getXPixel(), baseMapTile.getYPixel(), 5 * scale);
    }

    /**
     * Fetches the remote image with the params provided
     *
     * @param url            url of the image to fetch
     * @param method         method of the request
     * @param serializedBody body of the request, it does not get sent if null or empty
     * @param contentType    content type of the body, sent only if the body is provided
     * @return a bufferedImage containing the fetched data
     * @throws IOException   when there is an issue while fetching the image
     * @throws RestException when the response is anything but the image expected
     */
    private BufferedImage getImage(String url, String method, Map<String, String> headers, String serializedBody,
                                   String contentType, TileRapresentation tile) throws IOException, RestException {

        //TODO: user agent

        // apply url and body templates
        String finalUrl = spelService.applyTemplates(url, tileRadarConfig.getTemplates(), tile);
        String finalBody = spelService.applyTemplates(serializedBody, tileRadarConfig.getTemplates(), tile);

        // create a request body if configured
        RequestBody requestBody = null;
        if (finalBody != null && !finalBody.isBlank()) {
            requestBody = RequestBody.create(
                    MediaType.parse(contentType), finalBody
            );
        }

        Headers requestHeaders = Headers.of(headers);

        Request request = new Request.Builder()
                .url(finalUrl)
                .headers(requestHeaders)
                .method(method, requestBody)
                .build();

        // do the weather tile image request
        try (Response response = restService.executeRequest(request)) {
            ResponseBody responseBody = restService.validateResponse(response);

            // get image from body
            InputStream inputStream = responseBody.byteStream();
            return ImageIO.read(inputStream);
        }
    }

    /**
     * Given an imagery of a tile, crops and scales it to match the targetTile
     *
     * @param tileImagery the imagery to crop and scale in order to zoom-in
     * @param tile        the tile corresponding to the imagery
     * @param targetTile  a smaller tileRapresentation contained within the imagery, target of the zoom operation
     */
    private BufferedImage zoomTileImagery(BufferedImage tileImagery, TileRapresentation tile, TileRapresentation targetTile, int tilePixelSize) {

        // scale difference between representations
        int scale = (int) Math.pow(2, (double) targetTile.getZ() - tile.getZ());

        // extract lat long of target tile's corners for readability
        BigDecimal topLeftLatitude = targetTile.getTopLeftCorner().getLatitude();
        BigDecimal topLeftLongitude = targetTile.getTopLeftCorner().getLongitude();
        BigDecimal bottomRightLatitude = targetTile.getBottomRightCorner().getLatitude();
        BigDecimal bottomRightLongitude = targetTile.getBottomRightCorner().getLongitude();

        // targetTile is always contained inside the tile of the imagery.
        // the objective here is to identify the portion of the imagery that represents targetTile.
        // as such, we need to find the pixel index (xy) of each targetTile's corner inside of the imagery.

        // Calculate each corner tile representation.
        // this will result in a "recalculation" of the tile that represents the imagery 2 times,
        // but with the pixel index values (xPixel, yPixel) corresponding to targetTile's corner.
        TileRapresentation topLeftTile = TileUtils.latlongToTile(topLeftLatitude, topLeftLongitude, tile.getZ(), tilePixelSize);
        TileRapresentation bottomRightTile = TileUtils.latlongToTile(bottomRightLatitude, bottomRightLongitude, tile.getZ(), tilePixelSize);

        // Create the cropping boundary
        TileBoundary tileBoundary = TileUtils.getBoundaries(tileImagery.getWidth(), tileImagery.getHeight(), tile, topLeftTile, bottomRightTile);

        // do magic
        return ImageUtils.cropAndScale(tileImagery, tileBoundary, scale);
    }

}
