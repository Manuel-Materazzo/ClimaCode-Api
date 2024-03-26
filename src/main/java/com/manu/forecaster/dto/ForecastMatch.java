package com.manu.forecaster.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastMatch {
    private boolean matched;
    private List<String> matchedForecasts;
}
