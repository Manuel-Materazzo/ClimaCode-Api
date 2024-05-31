package com.manu.forecaster.dto.configuration;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TileRadarConfig {
    private String name;
    private List<TileRadarImageryConfig> imagery;
    private int zoomLevel;
    private float opacity;
    private Map<String, String> templates;
    private Map<String, String> headers;
    private Map<String, String> legend;
}
