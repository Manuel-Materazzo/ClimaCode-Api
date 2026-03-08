package com.manu.forecaster.service;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScrapersConfig;
import com.manu.forecaster.dto.forecast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastServiceTest {

    private ForecastService forecastService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    @BeforeEach
    void setUp() throws Exception {
        // Create config with all scrapers disabled to avoid real initialization
        WebScraperConfig disabledConfig = new WebScraperConfig();
        disabledConfig.setEnabled(false);

        WebScrapersConfig webScrapersConfig = new WebScrapersConfig();
        webScrapersConfig.setUserAgent("test-agent");
        webScrapersConfig.setMeteoblue(disabledConfig);
        webScrapersConfig.setMeteociel(disabledConfig);

        WeatherSourcesConfig weatherSourcesConfig = new WeatherSourcesConfig();
        weatherSourcesConfig.setWebScrapers(webScrapersConfig);

        forecastService = new ForecastService(weatherSourcesConfig, new RestService());
    }

    private void injectForecastServices(List<GenericForecastServiceInterface> services) throws Exception {
        Field field = ForecastService.class.getDeclaredField("forecastServices");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<GenericForecastServiceInterface> list = (List<GenericForecastServiceInterface>) field.get(forecastService);
        list.clear();
        list.addAll(services);
    }

    // --- getForecastRaw tests ---

    @Test
    void getForecastRaw_singleSource_returnsThatSource() throws Exception {
        Forecast forecast = Forecast.builder()
                .date(Instant.parse("2026-03-08T12:00:00Z"))
                .weatherCondition("rain")
                .weatherDescription("Light rain")
                .build();
        ForecastSource source = ForecastSource.builder()
                .name("TestSource")
                .forecasts(List.of(forecast))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);

        injectForecastServices(List.of(mockService));

        RawForecast result = forecastService.getForecastRaw("45.0", "9.0", Timeframe.TODAY);

        assertEquals(1, result.getSources().size());
        assertEquals("TestSource", result.getSources().get(0).getName());
        assertEquals(1, result.getSources().get(0).getForecasts().size());
        assertEquals("rain", result.getSources().get(0).getForecasts().get(0).getWeatherCondition());
    }

    @Test
    void getForecastRaw_multipleSources_allSourcesIncluded() throws Exception {
        ForecastSource source1 = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(Instant.now()).weatherCondition("sunny").build()))
                .build();
        ForecastSource source2 = ForecastSource.builder()
                .name("Source2")
                .forecasts(List.of(Forecast.builder().date(Instant.now()).weatherCondition("cloudy").build()))
                .build();

        GenericForecastServiceInterface mock1 = mock(GenericForecastServiceInterface.class);
        GenericForecastServiceInterface mock2 = mock(GenericForecastServiceInterface.class);
        when(mock1.getForecasts(Timeframe.TOMORROW, "40.0", "10.0")).thenReturn(source1);
        when(mock2.getForecasts(Timeframe.TOMORROW, "40.0", "10.0")).thenReturn(source2);

        injectForecastServices(List.of(mock1, mock2));

        RawForecast result = forecastService.getForecastRaw("40.0", "10.0", Timeframe.TOMORROW);

        assertEquals(2, result.getSources().size());
        assertEquals("Source1", result.getSources().get(0).getName());
        assertEquals("Source2", result.getSources().get(1).getName());
    }

    @Test
    void getForecastRaw_noSources_returnsEmptyRawForecast() throws Exception {
        injectForecastServices(List.of());

        RawForecast result = forecastService.getForecastRaw("45.0", "9.0", Timeframe.TODAY);

        assertNotNull(result.getSources());
        assertTrue(result.getSources().isEmpty());
    }

    // --- getForecastMatch tests ---

    @Test
    void getForecastMatch_matchingWeatherCondition_matchedIsTrue() throws Exception {
        Instant date = Instant.parse("2026-03-08T14:00:00Z");
        ForecastSource source = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(date).weatherCondition("rain").build()))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        ForecastMatch match = result.getMatches().get("rain");
        assertTrue(match.isMatched());
        assertEquals(1, match.getMatchedForecasts().size());
    }

    @Test
    void getForecastMatch_nonMatchingWeatherCondition_matchedIsFalse() throws Exception {
        ForecastSource source = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(Instant.now()).weatherCondition("sunny").build()))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        ForecastMatch match = result.getMatches().get("rain");
        assertFalse(match.isMatched());
        assertTrue(match.getMatchedForecasts().isEmpty());
    }

    @Test
    void getForecastMatch_nullWeatherCondition_notMatched() throws Exception {
        ForecastSource source = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(Instant.now()).weatherCondition(null).build()))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        ForecastMatch match = result.getMatches().get("rain");
        assertFalse(match.isMatched());
        assertTrue(match.getMatchedForecasts().isEmpty());
    }

    @Test
    void getForecastMatch_multipleWeatherTypes_onlyMatchingOnesAreTrue() throws Exception {
        ForecastSource source = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(
                        Forecast.builder().date(Instant.now()).weatherCondition("rain").build(),
                        Forecast.builder().date(Instant.now()).weatherCondition("sunny").build()
                ))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain", "snow"));

        assertTrue(result.getMatches().get("rain").isMatched());
        assertFalse(result.getMatches().get("snow").isMatched());
    }

    @Test
    void getForecastMatch_matchIncludesSourceNameAndFormattedDate() throws Exception {
        Instant date = Instant.parse("2026-03-08T14:00:00Z");
        ForecastSource source = ForecastSource.builder()
                .name("Meteoblue")
                .forecasts(List.of(Forecast.builder().date(date).weatherCondition("rain").build()))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        ForecastMatch match = result.getMatches().get("rain");
        String expectedMatchName = "Meteoblue " + formatter.format(date);
        assertEquals(1, match.getMatchedForecasts().size());
        assertEquals(expectedMatchName, match.getMatchedForecasts().get(0));
    }

    @Test
    void getForecastMatch_conditionContainsWeatherType_matchedByContains() throws Exception {
        ForecastSource source = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(Instant.now()).weatherCondition("heavy rain").build()))
                .build();

        GenericForecastServiceInterface mockService = mock(GenericForecastServiceInterface.class);
        when(mockService.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source);
        injectForecastServices(List.of(mockService));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        assertTrue(result.getMatches().get("rain").isMatched());
    }

    @Test
    void getForecastMatch_multipleSources_matchesAcrossSources() throws Exception {
        Instant date1 = Instant.parse("2026-03-08T10:00:00Z");
        Instant date2 = Instant.parse("2026-03-08T16:00:00Z");

        ForecastSource source1 = ForecastSource.builder()
                .name("Source1")
                .forecasts(List.of(Forecast.builder().date(date1).weatherCondition("rain").build()))
                .build();
        ForecastSource source2 = ForecastSource.builder()
                .name("Source2")
                .forecasts(List.of(Forecast.builder().date(date2).weatherCondition("rain").build()))
                .build();

        GenericForecastServiceInterface mock1 = mock(GenericForecastServiceInterface.class);
        GenericForecastServiceInterface mock2 = mock(GenericForecastServiceInterface.class);
        when(mock1.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source1);
        when(mock2.getForecasts(Timeframe.TODAY, "45.0", "9.0")).thenReturn(source2);
        injectForecastServices(List.of(mock1, mock2));

        WeatherMatchedForecast result = forecastService.getForecastMatch("45.0", "9.0", Timeframe.TODAY, List.of("rain"));

        ForecastMatch match = result.getMatches().get("rain");
        assertTrue(match.isMatched());
        assertEquals(2, match.getMatchedForecasts().size());
        assertEquals("Source1 " + formatter.format(date1), match.getMatchedForecasts().get(0));
        assertEquals("Source2 " + formatter.format(date2), match.getMatchedForecasts().get(1));
    }
}
