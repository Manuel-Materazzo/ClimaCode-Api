package com.manu.forecaster.dto.configuration;

import com.manu.forecaster.constant.Timeframe;
import lombok.*;

@Getter
@Setter
@With
@NoArgsConstructor
@AllArgsConstructor
public class WebScraperForecastsConfig {
    private String name;
    private String url;
    private Timeframe timeframe;
}
