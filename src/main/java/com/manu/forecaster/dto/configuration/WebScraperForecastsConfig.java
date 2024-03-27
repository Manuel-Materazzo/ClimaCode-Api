package com.manu.forecaster.dto.configuration;

import com.manu.forecaster.constant.Timeframe;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebScraperForecastsConfig {
    private String name;
    private String url;
    private Timeframe timeframe;
}
