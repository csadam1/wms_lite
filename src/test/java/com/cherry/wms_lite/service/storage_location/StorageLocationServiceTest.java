package com.cherry.wms_lite.service.storage_location;

import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.StorageLocationEntity;
import com.cherry.wms_lite.model.request.storage_location.StorageLocationRequest;
import com.cherry.wms_lite.model.response.storage_location.StorageLocationResponse;
import com.cherry.wms_lite.repository.StorageLocationRepository;
import com.cherry.wms_lite.service.inventory.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StorageLocationServiceTest {

    private static final String STORAGE_LOCATION_1 = "Storage Location 1";
    private static final String STORAGE_LOCATION_2 = "Storage Location 2";
    private static final String UPDATED_STORAGE_LOCATION = "Updated Location";
    private static final String DESCRIPTION_1 = "Description 1";
    private static final String DESCRIPTION_2 = "Description 2";
    private static final String UPDATED_DESCRIPTION = "Updated Description";
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final String SL_NOT_FOUND_WITH_NAME_EXCEPTION = "Storage Location not found with name: %s";
    private static final String SL_NOT_FOUND_WITH_ID_EXCEPTION = "Storage Location not found with id: %s";
    private static final String SL_WITH_NAME_EXIST_EXCEPTION = "Storage Location with name already exists: %s";

    @Mock
    private StorageLocationRepository storageLocationRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private Validator validator;

    @InjectMocks
    private StorageLocationService storageLocationService;

    @Test
    void getStorageLocationByName_success() {
        // Arrange
        StorageLocationEntity entity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        when(storageLocationRepository.findByName(STORAGE_LOCATION_1)).thenReturn(Optional.of(entity));

        // Act
        StorageLocationEntity result = storageLocationService.getStorageLocationByName(STORAGE_LOCATION_1);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.getId());
        assertEquals(STORAGE_LOCATION_1, result.getName());
        assertEquals(DESCRIPTION_1, result.getDescription());
        verify(storageLocationRepository, times(1)).findByName(STORAGE_LOCATION_1);
    }

    @Test
    void getStorageLocationByName_notFound() {
        // Arrange
        when(storageLocationRepository.findByName(STORAGE_LOCATION_1)).thenReturn(Optional.empty());

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> storageLocationService.getStorageLocationByName(STORAGE_LOCATION_1));

        assertEquals(SL_NOT_FOUND_WITH_NAME_EXCEPTION.formatted(STORAGE_LOCATION_1), exception.getMessage());
        verify(storageLocationRepository, times(1)).findByName(STORAGE_LOCATION_1);
    }

    @Test
    void getStorageLocationInventoryByName_success() {
        // Arrange
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).build();
        StorageLocationEntity entity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        entity.setInventoryEntity(inventory);
        when(storageLocationRepository.findByName(STORAGE_LOCATION_1)).thenReturn(Optional.of(entity));

        // Act
        InventoryEntity result = storageLocationService.getStorageLocationInventoryByName(STORAGE_LOCATION_1);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.getId());
        verify(storageLocationRepository, times(1)).findByName(STORAGE_LOCATION_1);
    }

    @Test
    void getStorageLocationInventoryByName_notFound() {
        // Arrange
        when(storageLocationRepository.findByName(STORAGE_LOCATION_1)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(EntityNotFoundException.class,
                () -> storageLocationService.getStorageLocationInventoryByName(STORAGE_LOCATION_1));

        verify(storageLocationRepository, times(1)).findByName(STORAGE_LOCATION_1);
    }

    @Test
    void getStorageLocationById_success() {
        // Arrange
        StorageLocationEntity entity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.of(entity));

        // Act
        StorageLocationResponse result = storageLocationService.getStorageLocationById(ID_1);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(STORAGE_LOCATION_1, result.name());
        assertEquals(DESCRIPTION_1, result.description());
        verify(storageLocationRepository, times(1)).findById(ID_1);
    }

    @Test
    void getStorageLocationById_notFound() {
        // Arrange
        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.empty());

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> storageLocationService.getStorageLocationById(ID_1));

        assertEquals(SL_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1), exception.getMessage());
        verify(storageLocationRepository, times(1)).findById(ID_1);
    }

    @Test
    void getAllStorageLocations_success() {
        // Arrange
        StorageLocationEntity entity1 = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        StorageLocationEntity entity2 = new StorageLocationEntity(ID_2, STORAGE_LOCATION_2, DESCRIPTION_2, null);
        List<StorageLocationEntity> entities = List.of(entity1, entity2);
        when(storageLocationRepository.findAll()).thenReturn(entities);

        // Act
        List<StorageLocationResponse> result = storageLocationService.getAllStorageLocations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ID_1, result.get(0).id());
        assertEquals(STORAGE_LOCATION_1, result.get(0).name());
        assertEquals(ID_2, result.get(1).id());
        assertEquals(STORAGE_LOCATION_2, result.get(1).name());
        verify(storageLocationRepository, times(1)).findAll();
    }

    @Test
    void getAllStorageLocations_emptyList() {
        // Arrange
        when(storageLocationRepository.findAll()).thenReturn(List.of());

        // Act
        List<StorageLocationResponse> result = storageLocationService.getAllStorageLocations();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(storageLocationRepository, times(1)).findAll();
    }

    @Test
    void createStorageLocation_success() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, DESCRIPTION_1);
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).build();
        StorageLocationEntity savedEntity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        savedEntity.setInventoryEntity(inventory);

        doNothing().when(validator).validateUniqueness(eq(STORAGE_LOCATION_1), any(), anyString());
        when(inventoryService.createNewInventory()).thenReturn(inventory);
        when(storageLocationRepository.save(any(StorageLocationEntity.class))).thenReturn(savedEntity);

        // Act
        StorageLocationResponse result = storageLocationService.createStorageLocation(request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(STORAGE_LOCATION_1, result.name());
        assertEquals(DESCRIPTION_1, result.description());
        verify(validator, times(1)).validateUniqueness(eq(STORAGE_LOCATION_1), any(), anyString());
        verify(inventoryService, times(1)).createNewInventory();
        verify(storageLocationRepository, times(1)).save(any(StorageLocationEntity.class));
    }

    @Test
    void createStorageLocation_nameAlreadyExists() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, DESCRIPTION_1);
        String errorMessage = SL_WITH_NAME_EXIST_EXCEPTION.formatted(STORAGE_LOCATION_1);

        doThrow(new IllegalArgumentException(errorMessage))
                .when(validator).validateUniqueness(eq(STORAGE_LOCATION_1), any(), eq(errorMessage));

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageLocationService.createStorageLocation(request));

        assertEquals(errorMessage, exception.getMessage());
        verify(inventoryService, never()).createNewInventory();
        verify(storageLocationRepository, never()).save(any(StorageLocationEntity.class));
    }

    @Test
    void updateStorageLocation_success() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(UPDATED_STORAGE_LOCATION, UPDATED_DESCRIPTION);
        StorageLocationEntity existingEntity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        StorageLocationEntity updatedEntity =
                new StorageLocationEntity(ID_1, UPDATED_STORAGE_LOCATION, UPDATED_DESCRIPTION, null);

        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(UPDATED_STORAGE_LOCATION)).thenReturn(false);
        when(validator.isNullOrEmpty(UPDATED_DESCRIPTION)).thenReturn(false);
        doNothing().when(validator).validateUniqueness(eq(UPDATED_STORAGE_LOCATION), any(), anyString());
        when(storageLocationRepository.save(any(StorageLocationEntity.class))).thenReturn(updatedEntity);

        // Act
        StorageLocationResponse result = storageLocationService.updateStorageLocation(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_STORAGE_LOCATION, result.name());
        assertEquals(UPDATED_DESCRIPTION, result.description());
        verify(storageLocationRepository, times(1)).findById(ID_1);
        verify(storageLocationRepository, times(1)).save(any(StorageLocationEntity.class));
    }

    @Test
    void updateStorageLocation_onlyName() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(UPDATED_STORAGE_LOCATION, null);
        StorageLocationEntity existingEntity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        StorageLocationEntity updatedEntity =
                new StorageLocationEntity(ID_1, UPDATED_STORAGE_LOCATION, DESCRIPTION_1, null);

        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(UPDATED_STORAGE_LOCATION)).thenReturn(false);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        doNothing().when(validator).validateUniqueness(eq(UPDATED_STORAGE_LOCATION), any(), anyString());
        when(storageLocationRepository.save(any(StorageLocationEntity.class))).thenReturn(updatedEntity);

        // Act
        StorageLocationResponse result = storageLocationService.updateStorageLocation(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_STORAGE_LOCATION, result.name());
        verify(storageLocationRepository, times(1)).findById(ID_1);
        verify(storageLocationRepository, times(1)).save(any(StorageLocationEntity.class));
    }

    @Test
    void updateStorageLocation_onlyDescription() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(null, UPDATED_DESCRIPTION);
        StorageLocationEntity existingEntity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        StorageLocationEntity updatedEntity =
                new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, UPDATED_DESCRIPTION, null);

        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(UPDATED_DESCRIPTION)).thenReturn(false);
        when(storageLocationRepository.save(any(StorageLocationEntity.class))).thenReturn(updatedEntity);

        // Act
        StorageLocationResponse result = storageLocationService.updateStorageLocation(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(STORAGE_LOCATION_1, result.name());
        assertEquals(UPDATED_DESCRIPTION, result.description());
        verify(storageLocationRepository, times(1)).findById(ID_1);
        verify(storageLocationRepository, times(1)).save(any(StorageLocationEntity.class));
        verify(validator, never()).validateUniqueness(anyString(), any(), anyString());
    }

    @Test
    void updateStorageLocation_notFound() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(UPDATED_STORAGE_LOCATION, UPDATED_DESCRIPTION);
        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.empty());

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> storageLocationService.updateStorageLocation(ID_1, request));

        assertEquals(SL_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1), exception.getMessage());
        verify(storageLocationRepository, times(1)).findById(ID_1);
        verify(storageLocationRepository, never()).save(any(StorageLocationEntity.class));
    }

    @Test
    void updateStorageLocation_nameAlreadyExists() {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, UPDATED_DESCRIPTION);
        StorageLocationEntity existingEntity = new StorageLocationEntity(ID_1, STORAGE_LOCATION_1, DESCRIPTION_1, null);
        String errorMessage = SL_WITH_NAME_EXIST_EXCEPTION.formatted(STORAGE_LOCATION_1);

        when(storageLocationRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(STORAGE_LOCATION_1)).thenReturn(false);
        doThrow(new IllegalArgumentException(errorMessage))
                .when(validator).validateUniqueness(eq(STORAGE_LOCATION_1), any(), eq(errorMessage));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageLocationService.updateStorageLocation(ID_1, request));

        assertEquals(errorMessage, exception.getMessage());
        verify(storageLocationRepository, times(1)).findById(ID_1);
        verify(storageLocationRepository, never()).save(any(StorageLocationEntity.class));
    }

    @Test
    void deleteStorageLocationById_success() {
        // Arrange
        when(storageLocationRepository.existsById(ID_1)).thenReturn(true);
        doNothing().when(storageLocationRepository).deleteById(ID_1);

        // Act
        storageLocationService.deleteStorageLocationById(ID_1);

        // Assert
        verify(storageLocationRepository, times(1)).existsById(ID_1);
        verify(storageLocationRepository, times(1)).deleteById(ID_1);
    }

    @Test
    void deleteStorageLocationById_notFound() {
        // Arrange
        when(storageLocationRepository.existsById(ID_1)).thenReturn(false);

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> storageLocationService.deleteStorageLocationById(ID_1));

        assertEquals(SL_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1), exception.getMessage());
        verify(storageLocationRepository, times(1)).existsById(ID_1);
        verify(storageLocationRepository, never()).deleteById(ID_1);
    }
}
