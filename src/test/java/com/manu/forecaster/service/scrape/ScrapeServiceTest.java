package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.exception.GeneralDataException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScrapeServiceTest {

	private static class TestableScrapeService extends ScrapeService {
		TestableScrapeService(WebScraperConfig config) {
			super("test-agent", config);
		}

		@Override
		public ForecastSource getForecasts(Timeframe timeframe, String latitude, String longitude) {
			return null;
		}
	}

	private WebScraperConfig buildConfig(List<WebScraperForecastsConfig> forecasts) {
		WebScraperConfig config = new WebScraperConfig();
		config.setForecasts(forecasts);
		return config;
	}

	@Test
	void forecastConfigFactory_exactTimeframeMatch_returnsMatchingConfig() {
		WebScraperForecastsConfig todayConfig =
				new WebScraperForecastsConfig("today", "https://example.com/{latitude}/{longitude}", Timeframe.TODAY);
		TestableScrapeService service = new TestableScrapeService(buildConfig(List.of(todayConfig)));

		WebScraperForecastsConfig result = service.forecastConfigFactory(Timeframe.TODAY, "45.0", "9.0");

		assertEquals("today", result.getName());
		assertEquals("https://example.com/45.0/9.0", result.getUrl());
		assertEquals(Timeframe.TODAY, result.getTimeframe());
	}

	@Test
	void forecastConfigFactory_noExactMatch_fallsBackToMultipleDays() {
		WebScraperForecastsConfig multiConfig =
				new WebScraperForecastsConfig("multi", "https://example.com/{latitude}/{longitude}", Timeframe.MULTIPLE_DAYS);
		TestableScrapeService service = new TestableScrapeService(buildConfig(List.of(multiConfig)));

		WebScraperForecastsConfig result = service.forecastConfigFactory(Timeframe.TOMORROW, "10.0", "20.0");

		assertEquals("multi", result.getName());
		assertEquals("https://example.com/10.0/20.0", result.getUrl());
		assertEquals(Timeframe.MULTIPLE_DAYS, result.getTimeframe());
	}

	@Test
	void forecastConfigFactory_noMatchAtAll_throwsGeneralDataException() {
		WebScraperForecastsConfig todayConfig =
				new WebScraperForecastsConfig("today", "https://example.com/{latitude}/{longitude}", Timeframe.TODAY);
		TestableScrapeService service = new TestableScrapeService(buildConfig(List.of(todayConfig)));

		GeneralDataException exception = assertThrows(GeneralDataException.class,
				() -> service.forecastConfigFactory(Timeframe.TOMORROW, "10.0", "20.0"));

		assertTrue(exception.getMessage().contains("TOMORROW"));
	}

	@Test
	void forecastConfigFactory_replacesUrlPlaceholders() {
		WebScraperForecastsConfig config =
				new WebScraperForecastsConfig("test", "https://weather.com/forecast?lat={latitude}&lon={longitude}", Timeframe.TODAY);
		TestableScrapeService service = new TestableScrapeService(buildConfig(List.of(config)));

		WebScraperForecastsConfig result = service.forecastConfigFactory(Timeframe.TODAY, "51.5074", "-0.1278");

		assertEquals("https://weather.com/forecast?lat=51.5074&lon=-0.1278", result.getUrl());
	}

	@Test
	void forecastConfigFactory_withUrlReturnsNewInstance_originalUnchanged() {
		WebScraperForecastsConfig originalConfig =
				new WebScraperForecastsConfig("test", "https://example.com/{latitude}/{longitude}", Timeframe.TODAY);
		TestableScrapeService service = new TestableScrapeService(buildConfig(List.of(originalConfig)));

		WebScraperForecastsConfig result = service.forecastConfigFactory(Timeframe.TODAY, "45.0", "9.0");

		assertNotSame(originalConfig, result);
		assertEquals("https://example.com/{latitude}/{longitude}", originalConfig.getUrl());
		assertEquals("https://example.com/45.0/9.0", result.getUrl());
	}
}
