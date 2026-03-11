package com.manu.forecaster.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class DisabledExceptionTest {

    @Test
    void constructorWithMessage_setsMessageAndDefaultStatus() {
        DisabledException ex = new DisabledException("feature disabled");

        assertEquals("feature disabled", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void constructorWithStatusAndMessage_setsBoth() {
        DisabledException ex = new DisabledException(HttpStatus.FORBIDDEN, "not allowed");

        assertEquals("not allowed", ex.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    void isRuntimeException() {
        DisabledException ex = new DisabledException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
