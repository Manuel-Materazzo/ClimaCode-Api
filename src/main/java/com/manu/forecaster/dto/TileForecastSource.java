package com.manu.forecaster.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TileForecastSource {
    private String sourceName;
    private List<TileForecast> forecast;
}
