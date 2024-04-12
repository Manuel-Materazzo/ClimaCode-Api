package com.manu.forecaster.dto.forecast;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastSource {
    private String name;
    private List<Forecast> forecasts;
}
