package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.exception.DisabledException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MeteblueScrapeServiceTest {

    private WebScraperConfig buildDisabledConfig() {
        WebScraperConfig config = new WebScraperConfig();
        config.setEnabled(false);
        config.setForecasts(List.of(
                new WebScraperForecastsConfig("today", "https://meteoblue.com/{latitude}/{longitude}", Timeframe.TODAY)
        ));
        config.setLegend(Map.of());
        return config;
    }

    @Test
    void getForecasts_whenDisabled_throwsDisabledException() {
        WebScraperConfig config = buildDisabledConfig();
        MeteblueScrapeService service = new MeteblueScrapeService("test-agent", config);

        DisabledException ex = assertThrows(DisabledException.class,
                () -> service.getForecasts(Timeframe.TODAY, "45.0", "9.0"));

        assertTrue(ex.getMessage().contains("MeteoBlue"));
    }

    @Test
    void getForecasts_whenDisabled_exceptionHasBadRequestStatus() {
        WebScraperConfig config = buildDisabledConfig();
        MeteblueScrapeService service = new MeteblueScrapeService("test-agent", config);

        DisabledException ex = assertThrows(DisabledException.class,
                () -> service.getForecasts(Timeframe.TODAY, "45.0", "9.0"));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }
}
