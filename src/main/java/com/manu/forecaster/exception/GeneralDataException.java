package com.manu.forecaster.exception;

import org.springframework.http.HttpStatus;

public class GeneralDataException extends RuntimeException  {

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public GeneralDataException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public GeneralDataException(String message) {
        super(message);
    }


}
