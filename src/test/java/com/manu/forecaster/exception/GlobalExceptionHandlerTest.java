package com.manu.forecaster.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneralDataException_customStatus() {
        GeneralDataException ex = new GeneralDataException(HttpStatus.EXPECTATION_FAILED, "custom error");

        ResponseEntity<Map<String, String>> response = handler.handleGeneralDataException(ex);

        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
        assertEquals("custom error", response.getBody().get("error"));
    }

    @Test
    void handleGeneralDataException_defaultStatus() {
        GeneralDataException ex = new GeneralDataException("default error");

        ResponseEntity<Map<String, String>> response = handler.handleGeneralDataException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("default error", response.getBody().get("error"));
    }

    @Test
    void handleDisabledException_defaultStatus() {
        DisabledException ex = new DisabledException("disabled error");

        ResponseEntity<Map<String, String>> response = handler.handleDisabledException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("disabled error", response.getBody().get("error"));
    }

    @Test
    void handleConfigurationException_defaultStatus() {
        ConfigurationException ex = new ConfigurationException("config error");

        ResponseEntity<Map<String, String>> response = handler.handleConfigurationException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("config error", response.getBody().get("error"));
    }

    @Test
    void handleNoSuchElementException() {
        NoSuchElementException ex = new NoSuchElementException("not found");

        ResponseEntity<Map<String, String>> response = handler.handleNoSuchElementException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().get("error"));
    }

    @Test
    void handleNumberFormatException() {
        NumberFormatException ex = new NumberFormatException("bad number");

        ResponseEntity<Map<String, String>> response = handler.handleNumberFormatException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad number", response.getBody().get("error"));
    }
}
