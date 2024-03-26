package com.manu.forecaster.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherMatchedForecast {
    private Map<String, ForecastMatch> matches;
}
