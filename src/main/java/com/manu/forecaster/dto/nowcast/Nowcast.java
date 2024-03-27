package com.manu.forecaster.dto.nowcast;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Nowcast {
    private String imageryName;
    private String pointWeatherCondition;
    private Map<String, Integer> areaWeatherCoinditions;
}
