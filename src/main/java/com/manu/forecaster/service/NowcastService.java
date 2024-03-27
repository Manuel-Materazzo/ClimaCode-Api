package com.manu.forecaster.service;

import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.dto.nowcast.*;
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

    public RawNowcast getNowcastsRaw(String latitude, String longitude) {

        ArrayList<NowcastSource> sources = new ArrayList<>();

        try {
            for (var tileRadarService : tileRadarServices) {
                List<Nowcast> nowcast = tileRadarService.getForecasts(new BigDecimal(latitude), new BigDecimal(longitude));
                NowcastSource nowcastSource = NowcastSource.builder()
                        .sourceName(tileRadarService.getName())
                        .nowcast(nowcast)
                        .build();
                sources.add(nowcastSource);
            }
        } catch (RestException | IOException e) {
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

        return RawNowcast.builder()
                .sources(sources)
                .build();
    }

    public WeatherMatchedNowcast getNowcastsMatch(String latitude, String longitude, List<String> weatherTypes) {

        Map<String, NowcastMatch> matches = initializeMatchesMap(weatherTypes);

        List<NowcastSource> nowcastSources = getNowcastsRaw(latitude, longitude).getSources();

        // for every weather radar source
        for (var source : nowcastSources) {
            List<Nowcast> nowcasts = source.getNowcast();
            // for every forecast tileset
            for (var forecast : nowcasts) {
                // iterate the weather types to check
                for (var weatherType : weatherTypes) {
                    // if the weather types are contained inside the forecast
                    if (GeneralUtils.mapContainsLike(forecast.getAreaWeatherCoinditions(), weatherType)) {
                        // add set match into the map
                        var match = matches.get(weatherType);
                        match.setAreaMatched(true);
                        String matchName = String.format("%s - %s", source.getSourceName(), forecast.getImageryName());
                        match.getMatchedForecasts().add(matchName);
                        // if the point weather matches the weather type, it's a point match
                        if (forecast.getPointWeatherCondition().contains(weatherType)) {
                            match.setPointMatched(true);
                        }
                    }
                }
            }
        }

        return WeatherMatchedNowcast.builder()
                .matches(matches)
                .build();
    }

    private Map<String, NowcastMatch> initializeMatchesMap(List<String> weatherTypes) {
        Map<String, NowcastMatch> map = new HashMap<>();

        for (var weatherType : weatherTypes) {
            NowcastMatch forecastMatch = NowcastMatch.builder()
                    .areaMatched(false)
                    .matchedForecasts(new ArrayList<>())
                    .build();
            map.put(weatherType, forecastMatch);
        }

        return map;
    }

}
