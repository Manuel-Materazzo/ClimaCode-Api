package com.manu.forecaster.dto.tile;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TileBoundary {
    private int topLeftXPixel;
    private int topLeftYPixel;
    private int bottomRightXPixel;
    private int bottomRightYPixel;
}
