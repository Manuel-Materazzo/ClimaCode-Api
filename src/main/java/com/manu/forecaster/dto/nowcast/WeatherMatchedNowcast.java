package com.manu.forecaster.dto.nowcast;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherMatchedNowcast {
    private Map<String, NowcastMatch> matches;
}
