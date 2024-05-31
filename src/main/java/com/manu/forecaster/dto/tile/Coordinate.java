package com.manu.forecaster.dto.tile;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private BigDecimal longitude;
    private BigDecimal latitude;
}
