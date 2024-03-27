package com.manu.forecaster.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Forecast {
    private Instant date;
    private String weatherCondition;
}
