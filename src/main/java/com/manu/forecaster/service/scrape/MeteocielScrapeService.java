package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.dto.forecast.Forecast;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.exception.DisabledException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class MeteocielScrapeService extends ScrapeService {

    public MeteocielScrapeService(String userAgent, WebScraperConfig config) {
        super(userAgent, config);
    }


    public ForecastSource getForecasts(Timeframe timeframe, String latitude, String longitude) {

        if (!config.isEnabled()) {
            throw new DisabledException("MeteoCiel forecast is not enabled");
        }

        WebScraperForecastsConfig currentForecastConfig = forecastConfigFactory(timeframe, latitude, longitude);

        // get web page
        Document doc = getDocument(currentForecastConfig.getUrl());

        // extract forecast table
        Elements forecastTable = doc.select("body > table:nth-child(2) > tbody > tr.texte > td:nth-child(2) > table > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(2) > td > center:nth-child(4) > table > tbody > tr > td:nth-child(1) > table:nth-child(1) > tbody > tr");

        // normalize data
        List<Forecast> forecasts = normalizeTable(forecastTable);

        String name = String.format("MeteoCiel - %s", currentForecastConfig.getName());

        return ForecastSource.builder()
                .name(name)
                .forecasts(forecasts)
                .build();
    }

    private List<Forecast> normalizeTable(Elements forecastTable) {

        // remove table header
        forecastTable.removeFirst();
        forecastTable.removeFirst();

        List<Forecast> forecasts = new ArrayList<>();
        int tempDay = 0; // stores the day to retrive it across columns

        // parse every table element into a forecast object and add it to the list
        for (Element element : forecastTable) {
            Elements row = element.select("td");

            // if the row contains the day, store and remove it
            if (row.size() == 11) {
                String dayRaw = row.getFirst().text();
                tempDay = Integer.parseInt(dayRaw.replaceAll("[^0-9]", ""));
                row.removeFirst();
            }

            // get the forecast date
            String hourString = row.getFirst().text();
            Instant utcDate = parseDateTime(tempDay, hourString);

            // get weather condition and description from the alt attribute on the img of the 9th td
            String weatherDescription = row.get(9).select("img").attr("alt");
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

    private Instant parseDateTime(int day, String hourstring) {

        // extract hour and minute
        String[] timestringComponents = hourstring.split(":");
        int hour = Integer.parseInt(timestringComponents[0]);
        int minute = Integer.parseInt(timestringComponents[1]);

        //TODO: offset should be based on lat long
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.of("+2"));

        // get current day number
        int currentDay = now.getDayOfMonth();

        // start by assuming that the forecast is for the current month/year
        int forecastMonth = now.getMonthValue();
        int forecastYear = now.getYear();

        // if the forecast has a lower day number than today, it's next month's forecast
        if (day < currentDay) {
            forecastMonth++;
            // if the new month is greater than 12, it's next year's forecast
            if (forecastMonth > 12) {
                forecastMonth = forecastMonth % 12;
                forecastYear++;
            }
        }

        LocalDateTime forecastTime = LocalTime.of(hour, minute).atDate(LocalDate.of(forecastYear, forecastMonth, day));

        // TODO: account for real LocalDateTime offset
        return forecastTime.toInstant(ZoneOffset.of("+2"));
    }

}
