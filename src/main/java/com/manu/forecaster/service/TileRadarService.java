package com.manu.forecaster.service;

import com.manu.forecaster.dto.TileForecast;
import com.manu.forecaster.dto.TileRapresentation;
import com.manu.forecaster.dto.configuration.TileRadarConfig;
import com.manu.forecaster.exception.RestException;
import com.manu.forecaster.utils.ImageUtils;
import com.manu.forecaster.utils.TileUtils;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TileRadarService {

    private final TileRadarConfig tileRadarConfig;
    private final RestService restService;
    private final SpelService spelService;

    public TileRadarService(TileRadarConfig tileRadarConfig, RestService restService, SpelService spelService) {
        this.tileRadarConfig = tileRadarConfig;
        this.restService = restService;
        this.spelService = spelService;
    }

    public List<TileForecast> getForecasts(BigDecimal latitude, BigDecimal longitude) throws IOException, RestException {

        List<TileForecast> forecasts = new ArrayList<>();

        for (var imagery : tileRadarConfig.getImagery()) {
            // calculate Tile and pixel position within tile
            TileRapresentation tile = TileUtils.latlongToTile(latitude, longitude, tileRadarConfig.getZoomLevel());

            // apply url and body templates
            String url = spelService.applyTemplates(imagery.getUrl(), tileRadarConfig.getTemplates(), tile);
            String serializedBody = spelService.applyTemplates(imagery.getBody(), tileRadarConfig.getTemplates(), tile);

            // get weather radar tile image
            BufferedImage weatherRadarTileImage = getImage(
                    url, imagery.getMethod(), tileRadarConfig.getHeaders(), serializedBody, imagery.getBodyContentType()
            );

            // get how many pixels around the point of interest have matching colors with the legend
            // pro tip: using a 5px search radius, we search on 100px, so you can read the pixel number as a coverage %
            Map<String, Integer> forecast = ImageUtils.getColorMatchCount(
                    weatherRadarTileImage, tileRadarConfig.getLegend(), tile.getXPixel(), tile.getYPixel(), 5
            );

            // add to the forecasts list
            forecasts.add(
                    TileForecast.builder()
                            .imageryName(imagery.getName())
                            .forecast(forecast)
                            .build()
            );
        }

        return forecasts;
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
                                   String contentType) throws IOException, RestException {

        // create a request body if configured
        RequestBody requestBody = null;
        if (serializedBody != null && !serializedBody.isBlank()) {
            requestBody = RequestBody.create(
                    MediaType.parse(contentType), serializedBody
            );
        }

        Headers requestHeaders = Headers.of(headers);

        Request request = new Request.Builder()
                .url(url)
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
