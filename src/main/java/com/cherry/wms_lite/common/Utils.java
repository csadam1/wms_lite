package com.cherry.wms_lite.common;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Utils {
    public String formatListToString(final List<?> list) {
        return list.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
