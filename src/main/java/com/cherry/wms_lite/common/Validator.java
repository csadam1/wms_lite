package com.cherry.wms_lite.common;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

@Service
public class Validator {
    public boolean isNullOrEmpty(final Object object) {
        return object == null || (object instanceof String str && str.trim().isEmpty());
    }

    public boolean isPositiveBigDecimal(final BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public <T> void validateUniqueness(final String value,
                                       final Function<String, Optional<T>> finderFunction,
                                       final String errorMessage)
    {
        finderFunction.apply(value)
                .ifPresent(entity -> {
                    throw new IllegalArgumentException(errorMessage);
                });
    }
}
