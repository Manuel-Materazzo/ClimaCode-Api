package com.manu.forecaster.utils;

import java.util.Map;

public class GeneralUtils {

    private GeneralUtils() {
    }

    public static boolean mapContainsLike(Map<String, Integer> map, String searchTerm) {

        // scan the map
        for (var entry : map.entrySet()) {
            // if the map contains the search terms, and the value is greater than zero
            if (entry.getKey().contains(searchTerm) && entry.getValue() > 0) {
                return true;
            }
        }

        return false;
    }
}
