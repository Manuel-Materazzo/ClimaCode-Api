package com.manu.forecaster.dto.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebScrapersConfig {
    private String userAgent;
    private WebScraperConfig meteoblue;
}
