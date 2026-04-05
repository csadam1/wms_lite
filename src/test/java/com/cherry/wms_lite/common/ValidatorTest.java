package com.cherry.wms_lite.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidatorTest {

    @InjectMocks
    private Validator validator;

    @Test
    void isNullOrEmpty_whenNull_shouldReturnTrue() {
        assertTrue(validator.isNullOrEmpty(null));
    }

    @Test
    void isNullOrEmpty_whenEmptyString_shouldReturnTrue() {
        assertTrue(validator.isNullOrEmpty(""));
        assertTrue(validator.isNullOrEmpty("   "));
    }

    @Test
    void isNullOrEmpty_whenNonEmptyString_shouldReturnFalse() {
        assertFalse(validator.isNullOrEmpty("test"));
    }

    @Test
    void isNullOrEmpty_whenNonStringObject_shouldReturnFalse() {
        assertFalse(validator.isNullOrEmpty(123));
        assertFalse(validator.isNullOrEmpty(new Object()));
    }

    @Test
    void isPositiveBigDecimal_whenNull_shouldReturnFalse() {
        assertFalse(validator.isPositiveBigDecimal(null));
    }

    @Test
    void isPositiveBigDecimal_whenZero_shouldReturnFalse() {
        assertFalse(validator.isPositiveBigDecimal(BigDecimal.ZERO));
    }

    @Test
    void isPositiveBigDecimal_whenNegative_shouldReturnFalse() {
        assertFalse(validator.isPositiveBigDecimal(BigDecimal.valueOf(-1)));
    }

    @Test
    void isPositiveBigDecimal_whenPositive_shouldReturnTrue() {
        assertTrue(validator.isPositiveBigDecimal(BigDecimal.valueOf(1)));
        assertTrue(validator.isPositiveBigDecimal(BigDecimal.valueOf(0.01)));
    }

    @Test
    void validateUniqueness_whenValueExists_shouldThrowException() {
        // Arrange
        Function<String, Optional<Object>> finderFunction = mock(Function.class);
        when(finderFunction.apply("test")).thenReturn(Optional.of(new Object()));

        // Act and Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUniqueness("test", finderFunction, "Value already exists")
        );

        assertEquals("Value already exists", exception.getMessage());
        verify(finderFunction).apply("test");
    }

    @Test
    void validateUniqueness_whenValueDoesNotExist_shouldNotThrowException() {
        // Arrange
        Function<String, Optional<Object>> finderFunction = mock(Function.class);
        when(finderFunction.apply("test")).thenReturn(Optional.empty());

        // Act and Assert
        assertDoesNotThrow(
                () -> validator.validateUniqueness("test", finderFunction, "Value already exists")
        );

        verify(finderFunction).apply("test");
    }
}