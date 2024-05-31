package com.manu.forecaster.dto.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "weather-sources")
public class WeatherSourcesConfig {
    private String baseMapUrl;
    private int baseMapZoomLevel;
    private int baseMapSize;
    private String userAgent;
    private List<TileRadarConfig> tileRadars;
    private WebScrapersConfig webScrapers;
}
