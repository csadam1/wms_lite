package com.cherry.wms_lite.service.inventory;

import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void createNewInventory_success() {
        // Arrange
        InventoryEntity savedInventory = InventoryEntity.builder()
                .id(1L)
                .build();

        when(inventoryRepository.save(any(InventoryEntity.class))).thenReturn(savedInventory);

        // Act
        InventoryEntity result = inventoryService.createNewInventory();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    void createNewInventory_repositoryThrowsDataAccessException() {
        // Arrange
        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> inventoryService.createNewInventory());

        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    void createNewInventory_repositoryThrowsRuntimeException() {
        // Arrange
        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryService.createNewInventory());

        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }
}
