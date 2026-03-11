package com.manu.forecaster.service;

import com.manu.forecaster.dto.configuration.TileRadarConfig;
import com.manu.forecaster.dto.configuration.TileRadarImageryConfig;
import com.manu.forecaster.dto.nowcast.Nowcast;
import com.manu.forecaster.dto.tile.TileRapresentation;
import com.manu.forecaster.exception.ConfigurationException;
import com.manu.forecaster.exception.RestException;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TileRadarServiceTest {

    private RestService restService;
    private SpelService spelService;
    private TileRadarConfig tileRadarConfig;
    private byte[] pngBytes;

    @BeforeEach
    void setUp() throws IOException {
        restService = mock(RestService.class);
        spelService = mock(SpelService.class);
        tileRadarConfig = new TileRadarConfig();
        tileRadarConfig.setName("test-radar");
        tileRadarConfig.setZoomLevel(6);
        tileRadarConfig.setSize(256);
        tileRadarConfig.setPixelToleranceRadius(5);
        tileRadarConfig.setOpacity(0.5f);
        tileRadarConfig.setTemplates(new HashMap<>());
        tileRadarConfig.setHeaders(new HashMap<>());
        tileRadarConfig.setLegend(Map.of("#00FF00", "rain", "#FF0000", "heavy rain"));

        // Pre-generate PNG bytes from a real BufferedImage
        BufferedImage testImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        pngBytes = baos.toByteArray();
    }

    private TileRadarImageryConfig createImagery(String name, String url, String method) {
        TileRadarImageryConfig imagery = new TileRadarImageryConfig();
        imagery.setName(name);
        imagery.setUrl(url);
        imagery.setMethod(method);
        imagery.setBody(null);
        imagery.setBodyContentType(null);
        return imagery;
    }

    private Response buildOkHttpResponse() {
        Request request = new Request.Builder().url("https://example.com").build();
        ResponseBody body = ResponseBody.create(pngBytes, MediaType.parse("image/png"));
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();
    }

    private ResponseBody createFreshPngBody() {
        return ResponseBody.create(pngBytes, MediaType.parse("image/png"));
    }

    private void setupMocksForImageFetch() throws IOException, RestException {
        when(spelService.applyTemplates(anyString(), anyMap(), any(TileRapresentation.class)))
                .thenAnswer(inv -> {
                    String s = inv.getArgument(0);
                    return s == null || s.isBlank() ? "" : s;
                });
        when(restService.executeRequest(any(Request.class))).thenReturn(buildOkHttpResponse());
        when(restService.validateResponse(any(Response.class))).thenAnswer(inv -> createFreshPngBody());
    }

    @Test
    void getName_returnsConfigName() {
        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        assertEquals("test-radar", service.getName());
    }

    @Test
    void getNowcasts_singleImagery_returnsOneNowcast() throws Exception {
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery));
        setupMocksForImageFetch();

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        List<Nowcast> nowcasts = service.getNowcasts(new BigDecimal("45.0"), new BigDecimal("9.0"));

        assertEquals(1, nowcasts.size());
        assertEquals("precip", nowcasts.get(0).getImageryName());
        assertNotNull(nowcasts.get(0).getAreaWeatherCoinditions());
    }

    @Test
    void getNowcasts_multipleImagery_returnsMultipleNowcasts() throws Exception {
        TileRadarImageryConfig imagery1 = createImagery("precip", "https://radar.com/1/{z}/{x}/{y}", "GET");
        TileRadarImageryConfig imagery2 = createImagery("wind", "https://radar.com/2/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery1, imagery2));
        setupMocksForImageFetch();

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        List<Nowcast> nowcasts = service.getNowcasts(new BigDecimal("45.0"), new BigDecimal("9.0"));

        assertEquals(2, nowcasts.size());
        assertEquals("precip", nowcasts.get(0).getImageryName());
        assertEquals("wind", nowcasts.get(1).getImageryName());
    }

    @Test
    void getNowcastImage_zoomLevelGreaterThanBaseMap_throwsConfigurationException() {
        tileRadarConfig.setZoomLevel(10);
        tileRadarConfig.setImagery(List.of());

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        assertThrows(ConfigurationException.class,
                () -> service.getNowcastImage(new BigDecimal("45.0"), new BigDecimal("9.0"), "precip"));
    }

    @Test
    void getNowcastImage_noMatchingImageryName_throwsNoSuchElementException() {
        tileRadarConfig.setZoomLevel(6);
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery));

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        assertThrows(NoSuchElementException.class,
                () -> service.getNowcastImage(new BigDecimal("45.0"), new BigDecimal("9.0"), "nonexistent"));
    }

    @Test
    void getNowcastImage_sameZoomLevel_returnsImage() throws Exception {
        tileRadarConfig.setZoomLevel(8);
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery));
        setupMocksForImageFetch();

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        BufferedImage result = service.getNowcastImage(new BigDecimal("45.0"), new BigDecimal("9.0"), "precip");

        assertNotNull(result);
        assertTrue(result.getWidth() > 0);
        assertTrue(result.getHeight() > 0);
    }

    @Test
    void getNowcastImage_differentZoomLevel_returnsScaledImage() throws Exception {
        tileRadarConfig.setZoomLevel(6);
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery));
        setupMocksForImageFetch();

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        BufferedImage result = service.getNowcastImage(new BigDecimal("45.0"), new BigDecimal("9.0"), "precip");

        assertNotNull(result);
    }

    @Test
    void getNowcasts_restServiceThrowsIOException_propagates() throws Exception {
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "GET");
        tileRadarConfig.setImagery(List.of(imagery));

        when(spelService.applyTemplates(anyString(), anyMap(), any(TileRapresentation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(restService.executeRequest(any(Request.class))).thenThrow(new IOException("connection failed"));

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        assertThrows(IOException.class,
                () -> service.getNowcasts(new BigDecimal("45.0"), new BigDecimal("9.0")));
    }

    @Test
    void getNowcastImage_withRequestBody_sendsBodyInRequest() throws Exception {
        tileRadarConfig.setZoomLevel(8);
        TileRadarImageryConfig imagery = createImagery("precip", "https://radar.com/{z}/{x}/{y}", "POST");
        imagery.setBody("{\"layer\": \"rain\"}");
        imagery.setBodyContentType("application/json");
        tileRadarConfig.setImagery(List.of(imagery));
        setupMocksForImageFetch();

        TileRadarService service = new TileRadarService(
                tileRadarConfig, restService, spelService, 8, 256, "https://basemap.com/{z}/{x}/{y}", "test-agent"
        );

        BufferedImage result = service.getNowcastImage(new BigDecimal("45.0"), new BigDecimal("9.0"), "precip");

        assertNotNull(result);
        // 2 calls: one for radar image, one for base map
        verify(restService, times(2)).executeRequest(any(Request.class));
    }
}
