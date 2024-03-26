package com.manu.forecaster.service;

import com.manu.forecaster.dto.RawForecast;
import com.manu.forecaster.dto.TileForecast;
import com.manu.forecaster.dto.TileForecastSource;
import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.exception.RestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public RawForecast getForecastsRaw(String latitude, String longitude) throws RestException, IOException {

        ArrayList<TileForecastSource> sources = new ArrayList<>();

        for (var tileRadarService : tileRadarServices) {
            List<TileForecast> forecast = tileRadarService.getForecasts(new BigDecimal(latitude), new BigDecimal(longitude));
            TileForecastSource forecastSource = TileForecastSource.builder()
                    .sourceName(tileRadarService.getName())
                    .forecast(forecast)
                    .build();
            sources.add(forecastSource);
        }

        return RawForecast.builder()
                .sources(sources)
                .build();
    }

}
