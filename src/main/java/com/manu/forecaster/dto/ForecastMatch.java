package com.manu.forecaster.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastMatch {
    private boolean pointMatched;
    private boolean areaMatched;
    private List<String> matchedForecasts;
}
