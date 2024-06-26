package com.manu.forecaster.controller;

import com.manu.forecaster.dto.nowcast.RawNowcast;
import com.manu.forecaster.dto.nowcast.WeatherMatchedNowcast;
import com.manu.forecaster.service.NowcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<RawNowcast> raw(@RequestParam String latitude, @RequestParam String longitude) {
        return ResponseEntity.ok(nowcastService.getNowcastsRaw(latitude, longitude));
    }

    @GetMapping({"/weatherMatch"})
    public ResponseEntity<WeatherMatchedNowcast> weatherMatch(@RequestParam String latitude, @RequestParam String longitude,
                                                              @RequestParam List<String> weatherTypes) {
        return ResponseEntity.ok(nowcastService.getNowcastsMatch(latitude, longitude, weatherTypes));
    }

    @GetMapping(path = "/radarImage", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> radarImage(@RequestParam String latitude, @RequestParam String longitude,
                                        @RequestParam String name) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(nowcastService.getImage(latitude, longitude, name));
    }

}
