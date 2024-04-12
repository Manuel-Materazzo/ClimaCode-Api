package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.forecast.Forecast;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.exception.DisabledException;
import com.manu.forecaster.exception.GeneralDataException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MeteblueScrapeService extends ScrapeService {

    public MeteblueScrapeService(String userAgent, WebScraperConfig config) {
        super(userAgent, config);
    }


    public ForecastSource getForecasts(Timeframe timeframe, String latitude, String longitude) {

        if (!config.isEnabled()) {
            throw new DisabledException("MeteoBlue forecast is not enabled");
        }

        WebScraperForecastsConfig currentForecastConfig = forecastConfigFactory(timeframe, latitude, longitude);

        // get web page
        Document doc = getDocument(currentForecastConfig.getUrl());

        // extract forecast element list
        Elements timeTable = doc.select(".three-hourly-view:not(.sea-surf-table) tbody tr.times td time");
        Elements iconTable = doc.select(".three-hourly-view:not(.sea-surf-table) tbody tr.icons td img");

        // normalize data
        List<Forecast> forecasts = normalizeTable(timeTable, iconTable);

        String name = String.format("MeteoBlue - %s", currentForecastConfig.getName());

        return ForecastSource.builder()
                .name(name)
                .forecasts(forecasts)
                .build();
    }

    private List<Forecast> normalizeTable(Elements timeTable, Elements iconTable) {
        List<Forecast> forecasts = new ArrayList<>();

        // tables should always contain the same amount of data
        if (timeTable.size() != iconTable.size()) {
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, "Meteoblue time and forecast icon mismatch on the table");
        }

        // parse every table element into a forecast object and add it to the list
        for (int i = 0; i < timeTable.size(); i++) {
            Element time = timeTable.get(i);
            Element icon = iconTable.get(i);

            String isoDateTime = time.attr("datetime");
            Instant utcDate = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(isoDateTime));

            String weatherDescription = icon.attr("title");
            String weatherCondition = config.getLegend().get(weatherDescription);

            Forecast forecast = Forecast.builder()
                    .date(utcDate)
                    .weatherCondition(weatherCondition)
                    .weatherDescription(weatherDescription)
                    .build();
            forecasts.add(forecast);
        }

        return forecasts;
    }

}
