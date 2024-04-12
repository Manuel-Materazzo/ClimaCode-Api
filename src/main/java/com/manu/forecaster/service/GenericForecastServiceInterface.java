package com.manu.forecaster.service;

import com.manu.forecaster.constant.Timeframe;
import com.manu.forecaster.dto.forecast.ForecastSource;

public interface GenericForecastServiceInterface {
    public abstract ForecastSource getForecasts(Timeframe timeframe,  String latitude, String longitude);

}
