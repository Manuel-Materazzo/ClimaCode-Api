package com.manu.forecaster;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.SpringApplication;

class ForecasterApplicationTest {

    @Test
    void main_invokesSpringApplicationRun() {
        try (var mocked = mockStatic(SpringApplication.class)) {
            ForecasterApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(ForecasterApplication.class, new String[]{}));
        }
    }

    @Test
    void classCanBeInstantiated() {
        ForecasterApplication app = new ForecasterApplication();
        assertNotNull(app);
    }
}
