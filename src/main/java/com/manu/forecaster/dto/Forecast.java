package com.manu.forecaster.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Forecast {
    private String imageryName;
    private String pointWeatherCondition;
    private Map<String, Integer> areaWeatherCoinditions;
}
