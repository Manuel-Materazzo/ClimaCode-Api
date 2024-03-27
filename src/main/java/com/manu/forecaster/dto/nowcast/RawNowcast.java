package com.manu.forecaster.dto.nowcast;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RawNowcast {
    private List<NowcastSource> sources;
}
