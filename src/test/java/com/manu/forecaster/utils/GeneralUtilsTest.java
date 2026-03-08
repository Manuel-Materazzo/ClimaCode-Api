package com.manu.forecaster.utils;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneralUtilsTest {

    @Test
    void mapContainsLike_exactKeyMatchWithPositiveValue_returnsTrue() {
        Map<String, Integer> map = Map.of("rain", 5);
        assertTrue(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_partialKeyMatchWithPositiveValue_returnsTrue() {
        Map<String, Integer> map = Map.of("rain-heavy", 3);
        assertTrue(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_exactKeyMatchWithZeroValue_returnsFalse() {
        Map<String, Integer> map = Map.of("rain", 0);
        assertFalse(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_noKeyMatch_returnsFalse() {
        Map<String, Integer> map = Map.of("snow", 5);
        assertFalse(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_emptyMap_returnsFalse() {
        Map<String, Integer> map = Collections.emptyMap();
        assertFalse(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_multipleEntriesOnlyOneMatches_returnsTrue() {
        Map<String, Integer> map = Map.of("snow", 0, "rain-heavy", 2, "fog", 0);
        assertTrue(GeneralUtils.mapContainsLike(map, "rain"));
    }

    @Test
    void mapContainsLike_keyMatchWithNegativeValue_returnsFalse() {
        Map<String, Integer> map = Map.of("rain", -1);
        assertFalse(GeneralUtils.mapContainsLike(map, "rain"));
    }
}
