package com.manu.forecaster.dto.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WebScraperConfig {
    private boolean enabled;
    private String geolocationMethod;
    private String geolocationContentType;
    private String geolocationUrl;
    private String geolocationBody;
    private List<WebScraperForecastsConfig> forecasts;
    private Map<String, String> legend;
}
