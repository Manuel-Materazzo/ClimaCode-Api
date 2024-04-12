package com.manu.forecaster.service;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WeatherSourcesConfig;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.forecast.*;
import com.manu.forecaster.service.scrape.MeteblueScrapeService;
import com.manu.forecaster.service.scrape.MeteocielScrapeService;
import com.manu.forecaster.service.scrape.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("singleton")
public class ForecastService {

    List<GenericForecastServiceInterface> forecastServices = new ArrayList<>();

    @Autowired
    ForecastService(WeatherSourcesConfig weatherSourcesConfig) {

        // initialize scrapers
        List<ScrapeService> scrapers = getScrapers(weatherSourcesConfig);
        forecastServices.addAll(scrapers);

        //TODO: api
    }

    public RawForecast getForecastRaw(String latitude, String longitude, Timeframe timeframe) {

        List<ForecastSource> forecastSources = new ArrayList<>();

        for(var forecastService: forecastServices){
            ForecastSource forecastSource = forecastService.getForecasts(timeframe, latitude, longitude);
            forecastSources.add(forecastSource);
        }

        return RawForecast.builder()
                .sources(forecastSources)
                .build();
    }

    public WeatherMatchedForecast getForecastMatch(String latitude, String longitude, Timeframe timeframe, List<String> weatherTypes) {

        Map<String, ForecastMatch> matches = initializeMatchesMap(weatherTypes);

        List<ForecastSource> forecastSources = getForecastRaw(latitude, longitude, timeframe).getSources();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        // for every forecast source
        for (var source : forecastSources) {
            List<Forecast> forecasts = source.getForecasts();
            // for every forecast tileset
            for (var forecast : forecasts) {
                // iterate the weather types to check
                for (var weatherType : weatherTypes) {
                    // if the weather types are contained inside the forecast
                    if (forecast.getWeatherCondition().contains(weatherType)) {
                        // add set match into the map
                        var match = matches.get(weatherType);
                        match.setMatched(true);
                        String matchName = String.format("%s %s", source.getName(), formatter.format(forecast.getDate()));
                        match.getMatchedForecasts().add(matchName);
                    }
                }
            }
        }

        return WeatherMatchedForecast.builder()
                .matches(matches)
                .build();
    }


    /**
     * Initialize the required web scrapers and returns them
     * @param weatherSourcesConfig config of the scrapers to initialize
     * @return a list of initialized scrapers
     */
    private List<ScrapeService> getScrapers(WeatherSourcesConfig weatherSourcesConfig) {
        String userAgent = weatherSourcesConfig.getWebScrapers().getUserAgent();

        List<ScrapeService> scrapeServices = new ArrayList<>();

        WebScraperConfig meteoblueConfig = weatherSourcesConfig.getWebScrapers().getMeteoblue();
        if (meteoblueConfig.isEnabled()) {
            ScrapeService meteoblueScraper = new MeteblueScrapeService(userAgent, meteoblueConfig);
            scrapeServices.add(meteoblueScraper);
        }

        WebScraperConfig meteocielConfig = weatherSourcesConfig.getWebScrapers().getMeteociel();
        if (meteocielConfig.isEnabled()) {
            ScrapeService meteocielScraper = new MeteocielScrapeService(userAgent, meteocielConfig);
            scrapeServices.add(meteocielScraper);
        }

        return scrapeServices;
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
