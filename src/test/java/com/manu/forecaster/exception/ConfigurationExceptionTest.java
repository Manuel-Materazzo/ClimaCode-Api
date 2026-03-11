package com.manu.forecaster.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationExceptionTest {

    @Test
    void constructorWithMessage_setsMessageAndDefaultStatus() {
        ConfigurationException ex = new ConfigurationException("bad config");

        assertEquals("bad config", ex.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
    }

    @Test
    void constructorWithStatusAndMessage_setsBoth() {
        ConfigurationException ex = new ConfigurationException(HttpStatus.BAD_REQUEST, "invalid");

        assertEquals("invalid", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void isRuntimeException() {
        ConfigurationException ex = new ConfigurationException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
