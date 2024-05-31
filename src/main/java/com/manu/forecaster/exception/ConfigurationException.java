package com.manu.forecaster.exception;

import org.springframework.http.HttpStatus;

public class ConfigurationException extends RuntimeException  {

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ConfigurationException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ConfigurationException(String message) {
        super(message);
    }


}
