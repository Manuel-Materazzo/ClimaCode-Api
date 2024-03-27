package com.manu.forecaster.dto.nowcast;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NowcastSource {
    private String sourceName;
    private List<Nowcast> nowcast;
}
