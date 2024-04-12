package com.manu.forecaster.dto.forecast;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RawForecast {
    private List<ForecastSource> sources;
}
