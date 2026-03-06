package com.manu.forecaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralDataException.class)
    public ResponseEntity<Map<String, String>> handleGeneralDataException(GeneralDataException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<Map<String, String>> handleConfigurationException(ConfigurationException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Map<String, String>> handleNumberFormatException(NumberFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

}
