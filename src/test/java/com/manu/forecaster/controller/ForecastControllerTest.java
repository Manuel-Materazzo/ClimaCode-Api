package com.manu.forecaster.controller;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.forecast.Forecast;
import com.manu.forecaster.dto.forecast.ForecastMatch;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.dto.forecast.RawForecast;
import com.manu.forecaster.dto.forecast.WeatherMatchedForecast;
import com.manu.forecaster.service.ForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastControllerTest {

    private final ForecastService forecastService = mock(ForecastService.class);
    private final ForecastController controller = new ForecastController(forecastService);

    @Test
    void raw_delegatesToServiceAndReturns200() {
        RawForecast expected = RawForecast.builder()
                .sources(List.of(ForecastSource.builder()
                        .name("Source1")
                        .forecasts(List.of(Forecast.builder()
                                .date(Instant.now())
                                .weatherCondition("rain")
                                .build()))
                        .build()))
                .build();
        when(forecastService.getForecastRaw("45.0", "9.0", Timeframe.TODAY)).thenReturn(expected);

        ResponseEntity<RawForecast> response = controller.raw("45.0", "9.0", Timeframe.TODAY);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(forecastService).getForecastRaw("45.0", "9.0", Timeframe.TODAY);
    }

    @Test
    void weatherMatch_delegatesToServiceAndReturns200() {
        WeatherMatchedForecast expected = WeatherMatchedForecast.builder()
                .matches(Map.of("rain", ForecastMatch.builder()
                        .matched(true)
                        .matchedForecasts(new ArrayList<>(List.of("Source1 2026-03-08 14:00")))
                        .build()))
                .build();
        List<String> weatherTypes = List.of("rain");
        when(forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, weatherTypes)).thenReturn(expected);

        ResponseEntity<WeatherMatchedForecast> response = controller.weatherMatch("45.0", "9.0", Timeframe.TODAY, weatherTypes);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(forecastService).getForecastMatch("45.0", "9.0", Timeframe.TODAY, weatherTypes);
    }

    @Test
    void raw_passesCorrectTimeframe() {
        RawForecast expected = RawForecast.builder().sources(List.of()).build();
        when(forecastService.getForecastRaw("10.0", "20.0", Timeframe.TOMORROW)).thenReturn(expected);

        ResponseEntity<RawForecast> response = controller.raw("10.0", "20.0", Timeframe.TOMORROW);

        assertEquals(200, response.getStatusCode().value());
        verify(forecastService).getForecastRaw("10.0", "20.0", Timeframe.TOMORROW);
    }
}
