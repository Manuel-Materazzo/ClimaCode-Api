package com.manu.forecaster.service;

import com.manu.forecaster.dto.configuration.TileRadarImageryConfig;
import com.manu.forecaster.dto.nowcast.Nowcast;
import com.manu.forecaster.dto.tile.TileRapresentation;
import com.manu.forecaster.dto.configuration.TileRadarConfig;
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

    public TileRadarService(TileRadarConfig tileRadarConfig, RestService restService, SpelService spelService, String baseMapUrl) {
        this.tileRadarConfig = tileRadarConfig;
        this.restService = restService;
        this.spelService = spelService;
        this.baseMapUrl = baseMapUrl;
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
     * @param latitude
     * @param longitude
     * @param name name of the imagery of the radar
     * @return a bufferedImage containing radar image overlayed on the base map
     * @throws IOException
     * @throws RestException
     */
    public BufferedImage getNowcastImage(BigDecimal latitude, BigDecimal longitude, String name) throws IOException, RestException {
        Optional<TileRadarImageryConfig> optionalImagery = tileRadarConfig.getImagery().stream().filter(service -> name.contains(service.getName())).findFirst();
        TileRadarImageryConfig imagery = optionalImagery.orElseThrow();

        // calculate Tiles and pixel position within tile
        TileRapresentation weatherRadarTile = TileUtils.latlongToTile(latitude, longitude, tileRadarConfig.getZoomLevel(), 512);
        TileRapresentation baseMapTile = TileUtils.latlongToTile(latitude, longitude, 6, 512);

        // get weather radar tile image
        BufferedImage weatherRadarTileImage = getImage(
                imagery.getUrl(), imagery.getMethod(), tileRadarConfig.getHeaders(), imagery.getBody(), imagery.getBodyContentType(), weatherRadarTile
        );

        // get base map tile image
        BufferedImage baseMapImage = getImage(
                baseMapUrl, "GET", new HashMap<>(), null, null, baseMapTile
        );

        BufferedImage overlayedImage = ImageUtils.overlayImage(baseMapImage, weatherRadarTileImage, 0.7f);

        // TODO: rescale radius for the 9 zoom level
        return ImageUtils.drawSquare(overlayedImage, baseMapTile.getXPixel(), baseMapTile.getYPixel(), 5);
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

}
