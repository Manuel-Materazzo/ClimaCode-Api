package com.manu.forecaster.service;

import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.dto.nowcast.*;
import com.manu.forecaster.exception.GeneralDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NowcastServiceTest {

	private NowcastService nowcastService;
	private TileRadarService radarService1;
	private TileRadarService radarService2;

	@BeforeEach
	void setUp() throws Exception {
		WeatherSourcesConfig config = new WeatherSourcesConfig();
		config.setTileRadars(Collections.emptyList());

		nowcastService = new NowcastService(config, new RestService(), new SpelService());

		radarService1 = mock(TileRadarService.class);
		radarService2 = mock(TileRadarService.class);
	}

	private void injectRadarServices(TileRadarService... services) throws Exception {
		Field field = NowcastService.class.getDeclaredField("tileRadarServices");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<TileRadarService> list = (List<TileRadarService>) field.get(nowcastService);
		list.addAll(Arrays.asList(services));
	}

	@Test
	void getNowcastsRaw_singleRadar_returnsOneSource() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		Nowcast nowcast = Nowcast.builder()
				.imageryName("imagery1")
				.pointWeatherCondition("rain")
				.areaWeatherCoinditions(Map.of("rain", 5))
				.build();
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(nowcast));
		injectRadarServices(radarService1);

		RawNowcast result = nowcastService.getNowcastsRaw("45.0", "9.0");

		assertEquals(1, result.getSources().size());
		assertEquals("radar1", result.getSources().get(0).getSourceName());
		assertEquals(1, result.getSources().get(0).getNowcast().size());
		assertEquals("imagery1", result.getSources().get(0).getNowcast().get(0).getImageryName());
	}

	@Test
	void getNowcastsRaw_multipleRadars_returnsAllSources() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		when(radarService2.getName()).thenReturn("radar2");
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(Nowcast.builder().imageryName("img1").areaWeatherCoinditions(Map.of()).build()));
		when(radarService2.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(Nowcast.builder().imageryName("img2").areaWeatherCoinditions(Map.of()).build()));
		injectRadarServices(radarService1, radarService2);

		RawNowcast result = nowcastService.getNowcastsRaw("45.0", "9.0");

		assertEquals(2, result.getSources().size());
		assertEquals("radar1", result.getSources().get(0).getSourceName());
		assertEquals("radar2", result.getSources().get(1).getSourceName());
	}

	@Test
	void getNowcastsRaw_radarThrowsIOException_throwsGeneralDataException() throws Exception {
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenThrow(new IOException("connection failed"));
		injectRadarServices(radarService1);

		assertThrows(GeneralDataException.class, () -> nowcastService.getNowcastsRaw("45.0", "9.0"));
	}

	@Test
	void getNowcastsMatch_areaConditionsContainWeatherType_areaMatchedTrue() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		Nowcast nowcast = Nowcast.builder()
				.imageryName("imagery1")
				.pointWeatherCondition(null)
				.areaWeatherCoinditions(Map.of("heavy rain", 10))
				.build();
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(nowcast));
		injectRadarServices(radarService1);

		WeatherMatchedNowcast result = nowcastService.getNowcastsMatch("45.0", "9.0", List.of("rain"));

		NowcastMatch match = result.getMatches().get("rain");
		assertTrue(match.isAreaMatched());
		assertFalse(match.isPointMatched());
		assertEquals(1, match.getMatchedForecasts().size());
		assertEquals("radar1 - imagery1", match.getMatchedForecasts().get(0));
	}

	@Test
	void getNowcastsMatch_pointWeatherConditionContainsType_pointMatchedTrue() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		Nowcast nowcast = Nowcast.builder()
				.imageryName("imagery1")
				.pointWeatherCondition("light rain")
				.areaWeatherCoinditions(Map.of("rain", 3))
				.build();
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(nowcast));
		injectRadarServices(radarService1);

		WeatherMatchedNowcast result = nowcastService.getNowcastsMatch("45.0", "9.0", List.of("rain"));

		NowcastMatch match = result.getMatches().get("rain");
		assertTrue(match.isAreaMatched());
		assertTrue(match.isPointMatched());
	}

	@Test
	void getNowcastsMatch_noMatchingWeather_bothFalse() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		Nowcast nowcast = Nowcast.builder()
				.imageryName("imagery1")
				.pointWeatherCondition("clear")
				.areaWeatherCoinditions(Map.of("sunny", 5))
				.build();
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(nowcast));
		injectRadarServices(radarService1);

		WeatherMatchedNowcast result = nowcastService.getNowcastsMatch("45.0", "9.0", List.of("rain"));

		NowcastMatch match = result.getMatches().get("rain");
		assertFalse(match.isAreaMatched());
		assertFalse(match.isPointMatched());
		assertTrue(match.getMatchedForecasts().isEmpty());
	}

	@Test
	void getNowcastsMatch_multipleWeatherTypes_correctMatchesMap() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		Nowcast nowcast = Nowcast.builder()
				.imageryName("imagery1")
				.pointWeatherCondition("snow")
				.areaWeatherCoinditions(Map.of("snow", 8, "rain", 0, "hail", 2))
				.build();
		when(radarService1.getNowcasts(any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(List.of(nowcast));
		injectRadarServices(radarService1);

		WeatherMatchedNowcast result = nowcastService.getNowcastsMatch("45.0", "9.0", List.of("snow", "rain", "hail"));

		NowcastMatch snowMatch = result.getMatches().get("snow");
		assertTrue(snowMatch.isAreaMatched());
		assertTrue(snowMatch.isPointMatched());

		NowcastMatch rainMatch = result.getMatches().get("rain");
		assertFalse(rainMatch.isAreaMatched());
		assertFalse(rainMatch.isPointMatched());

		NowcastMatch hailMatch = result.getMatches().get("hail");
		assertTrue(hailMatch.isAreaMatched());
		assertFalse(hailMatch.isPointMatched());
	}

	@Test
	void getImage_validName_returnsByteArray() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		when(radarService1.getNowcastImage(any(BigDecimal.class), any(BigDecimal.class), eq("radar1")))
				.thenReturn(image);
		injectRadarServices(radarService1);

		byte[] result = nowcastService.getImage("45.0", "9.0", "radar1");

		assertNotNull(result);
		assertTrue(result.length > 0);
	}

	@Test
	void getImage_noMatchingService_throwsNoSuchElementException() throws Exception {
		when(radarService1.getName()).thenReturn("radar1");
		injectRadarServices(radarService1);

		assertThrows(NoSuchElementException.class, () -> nowcastService.getImage("45.0", "9.0", "unknown"));
	}

}
