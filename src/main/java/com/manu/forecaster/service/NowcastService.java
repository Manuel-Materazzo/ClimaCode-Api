package com.manu.forecaster.service;

import com.manu.forecaster.dto.*;
import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.exception.GeneralDataException;
import com.manu.forecaster.exception.RestException;
import com.manu.forecaster.utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("singleton")
public class NowcastService {

    private final List<TileRadarService> tileRadarServices = new ArrayList<>();

    @Autowired
    NowcastService(WeatherSourcesConfig weatherSourcesConfig, RestService restService, SpelService spelService) {
        // initialize tile radars
        for (var tileradar : weatherSourcesConfig.getTileRadars()) {
            TileRadarService trs = new TileRadarService(tileradar, restService, spelService, weatherSourcesConfig.getBaseMapUrl());
            tileRadarServices.add(trs);
        }

    }

    public RawForecast getForecastsRaw(String latitude, String longitude) {

        ArrayList<TileForecastSource> sources = new ArrayList<>();

        try {
            for (var tileRadarService : tileRadarServices) {
                List<TileForecast> forecast = tileRadarService.getForecasts(new BigDecimal(latitude), new BigDecimal(longitude));
                TileForecastSource forecastSource = TileForecastSource.builder()
                        .sourceName(tileRadarService.getName())
                        .forecast(forecast)
                        .build();
                sources.add(forecastSource);
            }
        } catch (RestException | IOException e) {
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

        return RawForecast.builder()
                .sources(sources)
                .build();
    }

    public WeatherMatchedForecast getForecastsMatch(String latitude, String longitude, List<String> weatherTypes) {

        Map<String, ForecastMatch> matches = initializeMatchesMap(weatherTypes);

        List<TileForecastSource> forecastSources = getForecastsRaw(latitude, longitude).getSources();

        // for every weather radar source
        for (var source : forecastSources) {
            List<TileForecast> forecasts = source.getForecast();
            // for every forecast tileset
            for (var forecast : forecasts) {
                // iterate the weather types to check
                for (var weatherType : weatherTypes) {
                    // if the weather types are contained inside the forecast
                    if (GeneralUtils.mapContainsLike(forecast.getWeatherCoinditions(), weatherType)) {
                        // add set match into the map
                        var match = matches.get(weatherType);
                        match.setMatched(true);
                        String matchName = String.format("%s - %s", source.getSourceName(), forecast.getImageryName());
                        match.getMatchedForecasts().add(matchName);
                    }
                }
            }
        }

        return WeatherMatchedForecast.builder()
                .matches(matches)
                .build();
    }

    private Map<String, ForecastMatch> initializeMatchesMap(List<String> weatherTypes) {
        Map<String, ForecastMatch> map = new HashMap<>();

        for (var weatherType : weatherTypes) {
            ForecastMatch forecastMatch = ForecastMatch.builder()
                    .matched(false)
                    .matchedForecasts(new ArrayList<>())
                    .build();
            map.put(weatherType, forecastMatch);
        }

        return map;
    }

}
