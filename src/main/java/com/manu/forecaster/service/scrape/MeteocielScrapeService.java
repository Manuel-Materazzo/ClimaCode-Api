package com.manu.forecaster.service.scrape;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.configuration.WebScraperConfig;
import com.manu.forecaster.dto.configuration.WebScraperForecastsConfig;
import com.manu.forecaster.dto.forecast.Forecast;
import com.manu.forecaster.dto.forecast.ForecastSource;
import com.manu.forecaster.exception.DisabledException;
import com.manu.forecaster.exception.GeneralDataException;
import com.manu.forecaster.exception.RestException;
import com.manu.forecaster.service.RestService;
import okhttp3.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeteocielScrapeService extends ScrapeService {

    private final RestService restService;

    public MeteocielScrapeService(RestService restService, String userAgent, WebScraperConfig config) {
        super(userAgent, config);
        this.restService = restService;
    }


    public ForecastSource getForecasts(Timeframe timeframe, String latitude, String longitude) {

        if (!config.isEnabled()) {
            throw new DisabledException("MeteoCiel forecast is not enabled");
        }

        WebScraperForecastsConfig currentForecastConfig = forecastConfigFactory(timeframe, latitude, longitude);

        String url = getGeolocatedUrl(currentForecastConfig.getUrl(), latitude, longitude);

        // get web page
        Document doc = getDocument(url);

        // extract forecast table
        Elements forecastTable = doc.select("body > table:nth-child(2) > tbody > tr.texte > td:nth-child(2) > table > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(2) > td > center:nth-child(4) > table > tbody > tr > td:nth-child(1) > table:nth-child(1) > tbody > tr");

        // extract timezone
        Elements timezoneElement = doc.select("body > table:nth-child(2) > tbody > tr.texte > td:nth-child(2) > table > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(2) > td > center:nth-child(4) > b");

        // normalize data
        List<Forecast> forecasts = normalizeTable(forecastTable, timezoneElement);

        String name = String.format("MeteoCiel - %s", currentForecastConfig.getName());

        return ForecastSource.builder()
                .name(name)
                .forecasts(forecasts)
                .build();
    }

    /**
     * Gets the forecast url by calling meteociel geolocation service to retrieve the location id
     * @param url url that needs its geolocation-id replaced
     * @param latitude latitude of the location point
     * @param longitude longitude of the location point
     * @return the initial url, with the geolocation-id replaced
     */
    private String getGeolocatedUrl(String url, String latitude, String longitude) {
        String geolocationUrl = config.getGeolocationUrl();
        String geolocationBody = config.getGeolocationBody();

        // replace lat and long on geolocator url
        geolocationUrl = geolocationUrl.replace("{latitude}", latitude);
        geolocationUrl = geolocationUrl.replace("{longitude}", longitude);


        // create a request body if configured
        RequestBody requestBody = null;
        if (geolocationBody != null && !geolocationBody.isBlank()) {
            // replace lat and long on geolocator body
            geolocationBody = geolocationBody.replace("{latitude}", latitude);
            geolocationBody = geolocationBody.replace("{longitude}", longitude);
            requestBody = RequestBody.create(
                    geolocationBody, MediaType.parse(config.getGeolocationContentType())
            );
        }

        // add content type to headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", config.getGeolocationContentType());
        Headers requestHeaders = Headers.of(headers);

        // call geolocation service to retrieve location id
        Request request = new Request.Builder()
                .url(geolocationUrl)
                .headers(requestHeaders)
                .method(config.getGeolocationMethod(), requestBody)
                .build();

        String locationId = "";

        try (Response res = restService.executeRequest(request)) {
            var responseBody = restService.validateResponse(res);
            // extract the location id from the response (location name|location id|some number)
            var bodyString = responseBody.string();
            locationId = bodyString.split("\\|")[1];

        } catch (IOException | RestException e) {
            e.printStackTrace();
            String message = String.format("Unable to get locationId from geolocation service: %s", e.getMessage());
            throw new GeneralDataException(HttpStatus.EXPECTATION_FAILED, message);
        }

        url = url.replace("{location-id}", locationId);

        return url;
    }

    /**
     * Extract a list of forecasts from the provided meteociel table
     * @param forecastTable table to extract forecast data from
     * @param timezoneElement element to extract the timezone from
     * @return a standardized list of forecasts
     */
    private List<Forecast> normalizeTable(Elements forecastTable, Elements timezoneElement) {

        // extract timezone
        String timezone = timezoneElement.text().replace("GMT", "");

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
            Instant utcDate = parseDateTime(tempDay, hourString, timezone);

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

    /**
     * Computes the Instant of a given day number and hourstring at the provided timezone
     * @param day the day of the instant
     * @param hourstring the hour of the instant (HH:mm)
     * @param timezone the timezone of the day and hour provided
     * @return an instant representing the given day and hour, in the current year/month context
     */
    private Instant parseDateTime(int day, String hourstring, String timezone) {

        // extract hour and minute
        String[] timestringComponents = hourstring.split(":");
        int hour = Integer.parseInt(timestringComponents[0]);
        int minute = Integer.parseInt(timestringComponents[1]);

        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.of(timezone));

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

        return forecastTime.toInstant(ZoneOffset.of(timezone));
    }

}
