package com.cherry.wms_lite.common;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class Validator {
    public boolean isNullOrEmpty(final Object object) {
        return object == null || (object instanceof String str && str.trim().isEmpty());
    }

    public boolean isPositiveBigDecimal(final BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
