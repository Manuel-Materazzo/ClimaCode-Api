package com.manu.forecaster.exception;

import org.springframework.http.HttpStatus;

public class DisabledException extends RuntimeException  {

    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public DisabledException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public DisabledException(String message) {
        super(message);
    }


}
