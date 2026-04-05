package com.cherry.wms_lite.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @ParameterizedTest(name = "formatListToString({1}) = \"{0}\"")
    @MethodSource("provideFormatListToStringCases")
    void formatListToString_check(final String expected, final List<?> input) {
        assertEquals(expected, Utils.formatListToString(input));
    }

    private static Stream<Arguments> provideFormatListToStringCases() {
        return Stream.of(
                Arguments.of("", null),
                Arguments.of("", List.of()),
                Arguments.of("1", List.of(1)),
                Arguments.of("1, 2, 3", List.of(1, 2, 3)),
                Arguments.of("a, b, c", List.of("a", "b", "c"))
        );
    }
}
