package com.manu.forecaster.dto.nowcast;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NowcastMatch {
    private boolean pointMatched;
    private boolean areaMatched;
    private List<String> matchedForecasts;
}
