package com.manu.forecaster.dto.configuration;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TileRadarConfig {
    private String url;
    private int zoomLevel;
    private String method;
    private String body;
    private String bodyContentType;
    private Map<String, String> templates;
    private Map<String, String> headers;
    private Map<String, String> legend;
}
