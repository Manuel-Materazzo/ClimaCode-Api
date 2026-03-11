package com.manu.forecaster.controller;

import com.manu.forecaster.dto.nowcast.*;
import com.manu.forecaster.service.NowcastService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NowcastControllerTest {

    private final NowcastService nowcastService = mock(NowcastService.class);
    private final NowcastController controller = new NowcastController(nowcastService);

    @Test
    void raw_delegatesToServiceAndReturns200() {
        RawNowcast expected = RawNowcast.builder()
                .sources(List.of(NowcastSource.builder()
                        .sourceName("radar1")
                        .nowcast(List.of(Nowcast.builder()
                                .imageryName("img1")
                                .areaWeatherCoinditions(Map.of("rain", 5))
                                .build()))
                        .build()))
                .build();
        when(nowcastService.getNowcastsRaw("45.0", "9.0")).thenReturn(expected);

        ResponseEntity<RawNowcast> response = controller.raw("45.0", "9.0");

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(nowcastService).getNowcastsRaw("45.0", "9.0");
    }

    @Test
    void weatherMatch_delegatesToServiceAndReturns200() {
        WeatherMatchedNowcast expected = WeatherMatchedNowcast.builder()
                .matches(Map.of("rain", NowcastMatch.builder()
                        .areaMatched(true)
                        .pointMatched(false)
                        .matchedForecasts(new ArrayList<>(List.of("radar1 - img1")))
                        .build()))
                .build();
        List<String> weatherTypes = List.of("rain");
        when(nowcastService.getNowcastsMatch("45.0", "9.0", weatherTypes)).thenReturn(expected);

        ResponseEntity<WeatherMatchedNowcast> response = controller.weatherMatch("45.0", "9.0", weatherTypes);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(nowcastService).getNowcastsMatch("45.0", "9.0", weatherTypes);
    }

    @Test
    void radarImage_delegatesToServiceAndReturns200() {
        byte[] imageBytes = new byte[]{1, 2, 3};
        when(nowcastService.getImage("45.0", "9.0", "radar1")).thenReturn(imageBytes);

        ResponseEntity<?> response = controller.radarImage("45.0", "9.0", "radar1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(imageBytes, response.getBody());
        verify(nowcastService).getImage("45.0", "9.0", "radar1");
    }

    @Test
    void raw_passesCoordinatesCorrectly() {
        RawNowcast expected = RawNowcast.builder().sources(List.of()).build();
        when(nowcastService.getNowcastsRaw("-33.87", "151.21")).thenReturn(expected);

        ResponseEntity<RawNowcast> response = controller.raw("-33.87", "151.21");

        assertEquals(200, response.getStatusCode().value());
        verify(nowcastService).getNowcastsRaw("-33.87", "151.21");
    }
}
