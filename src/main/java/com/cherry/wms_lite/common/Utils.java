package com.cherry.wms_lite.common;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

public final class Utils {
    private static final String SEPARATOR = ", ";

    private Utils() {}

    public static String formatListToString(final List<?> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(SEPARATOR));
    }
}
