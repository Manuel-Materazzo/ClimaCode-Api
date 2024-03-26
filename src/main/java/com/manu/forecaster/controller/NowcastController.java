package com.manu.forecaster.controller;

import com.manu.forecaster.dto.RawForecast;
import com.manu.forecaster.dto.WeatherMatchedForecast;
import com.manu.forecaster.service.NowcastService;
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
        path = "/nowcast",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class NowcastController {


    private final NowcastService nowcastService;

    @Autowired
    NowcastController(NowcastService nowcastService) {
        this.nowcastService = nowcastService;
    }


    @GetMapping({"/raw"})
    public ResponseEntity<RawForecast> raw(@RequestParam String latitude, @RequestParam String longitude) {
        return ResponseEntity.ok(nowcastService.getForecastsRaw(latitude, longitude));
    }

    @GetMapping({"/weatherMatch"})
    public ResponseEntity<WeatherMatchedForecast> weatherMatch(@RequestParam String latitude, @RequestParam String longitude,
                                                               @RequestParam List<String> weatherTypes) {
        return ResponseEntity.ok(nowcastService.getForecastsMatch(latitude, longitude, weatherTypes));
    }

}
