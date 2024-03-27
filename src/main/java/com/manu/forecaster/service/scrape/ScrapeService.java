package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.ForecastSource;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.exception.GeneralDataException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public abstract class ScrapeService {

    protected final String userAgent;
    protected final WebScraperConfig config;

    ScrapeService(String userAgent, WebScraperConfig config) {
        this.userAgent = userAgent;
        this.config = config;
    }

    public abstract ForecastSource getForecasts(Timeframe timeframe);

    /**
     * Scrapes the document at the provided url
     *
     * @param url scrape target
     * @return extracted Jsoup Document
     */
    protected Document getDocument(String url) {
        Document document;

        try {
            document = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .get();
        } catch (IOException e) {
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

        return document;
    }

    protected WebScraperForecastsConfig forecastConfigFactory(Timeframe timeframe) {
        for (var forecast : config.getForecasts()) {
            if (forecast.getTimeframe() == timeframe) {
                return forecast;
            }
        }

        String message = String.format("The forecast for the timeframe %s is not defined", timeframe.toString());
        throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, message);
    }
}
