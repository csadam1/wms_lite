package com.cherry.wms_lite.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void validateUniqueness_whenValueExists_shouldThrowException() {
        // Arrange
        Function<String, Optional<Object>> finderFunction = value -> Optional.of(new Object());

        // Act and Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUniqueness("test", finderFunction, "Value already exists")
        );

        assertEquals("Value already exists", exception.getMessage());
    }

    @Test
    void validateUniqueness_whenValueDoesNotExist_shouldNotThrowException() {
        // Arrange
        Function<String, Optional<Object>> finderFunction = value -> Optional.empty();

        // Act and Assert
        assertDoesNotThrow(
                () -> validator.validateUniqueness("test", finderFunction, "Value already exists"));
    }
}