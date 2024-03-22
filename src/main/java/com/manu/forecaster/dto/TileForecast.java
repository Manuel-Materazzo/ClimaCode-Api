package com.manu.forecaster.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TileForecast {
    private String imageryName;
    private Map<String, Integer> forecast;
}
