package com.manu.forecaster.controller;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.forecast.RawForecast;
import com.manu.forecaster.dto.forecast.WeatherMatchedForecast;
import com.manu.forecaster.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        path = "/forecast",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ForecastController {


    private final ForecastService forecastService;

    @Autowired
    ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }


    @GetMapping({"/raw"})
    public ResponseEntity<RawForecast> raw(@RequestParam String latitude, @RequestParam String longitude, @RequestParam Timeframe timeframe) {
        return ResponseEntity.ok(forecastService.getForecastRaw(latitude, longitude, timeframe));
    }

    @GetMapping({"/weatherMatch"})
    public ResponseEntity<WeatherMatchedForecast> weatherMatch(@RequestParam String latitude,
                                                               @RequestParam String longitude,
                                                               @RequestParam Timeframe timeframe,
                                                               @RequestParam List<String> weatherTypes) {
        return ResponseEntity.ok(forecastService.getForecastMatch(latitude, longitude, timeframe, weatherTypes));
    }

}
