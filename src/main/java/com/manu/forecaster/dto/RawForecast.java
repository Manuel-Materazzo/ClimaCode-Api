package com.manu.forecaster.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RawForecast {
    private List<TileForecastSource> sources;
}
