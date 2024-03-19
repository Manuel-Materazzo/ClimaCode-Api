package com.manu.forecaster.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TileRapresentation {
    private int x;
    private int y;
    private int z;
    private int xPixel;
    private int yPixel;
}
