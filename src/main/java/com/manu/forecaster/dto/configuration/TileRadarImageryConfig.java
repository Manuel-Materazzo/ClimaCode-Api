package com.manu.forecaster.dto.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TileRadarImageryConfig {
    private String name;
    private String url;
    private String method;
    private String body;
    private String bodyContentType;
}
