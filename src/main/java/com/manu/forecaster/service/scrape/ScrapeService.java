package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.exception.GeneralDataException;
import com.manu.forecaster.service.GenericForecastServiceInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public abstract class ScrapeService implements GenericForecastServiceInterface {

    protected final String userAgent;
    protected final WebScraperConfig config;

    ScrapeService(String userAgent, WebScraperConfig config) {
        this.userAgent = userAgent;
        this.config = config;
    }

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

    protected WebScraperForecastsConfig forecastConfigFactory(Timeframe timeframe, String latitude, String longitude) {

        WebScraperForecastsConfig configInstance = getWebScraperForecastsConfig(timeframe);

        // if there is no match for the timeframe, nor generic timeframes TODO: only warn and return null?
        if (configInstance == null) {
            String message = String.format("The forecast for the timeframe %s is not defined", timeframe.toString());
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, message);
        }

        // replace url placeholders
        String url = configInstance.getUrl();
        url = url.replace("{latitude}", latitude);
        url = url.replace("{longitude}", longitude);

        // instantiate a new config using the wither to replace the url
        return configInstance.withUrl(url);
    }

    private WebScraperForecastsConfig getWebScraperForecastsConfig(Timeframe timeframe) {
        WebScraperForecastsConfig configInstance = null;
        WebScraperForecastsConfig multiTimeframeConfigInstance = null;

        // search a forecast for the specified timeframe and a forecast for all timeframes
        for (var forecast : config.getForecasts()) {
            if (forecast.getTimeframe() == timeframe) {
                configInstance = forecast;
            }
            if (forecast.getTimeframe() == Timeframe.MULTIPLE_DAYS) {
                multiTimeframeConfigInstance = forecast;
            }
        }

        // if there is no a precise match for the timeframe, use the generic one
        if (configInstance == null) {
            configInstance = multiTimeframeConfigInstance;
        }
        return configInstance;
    }
}
