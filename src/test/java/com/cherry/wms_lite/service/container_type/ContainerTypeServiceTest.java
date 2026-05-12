package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ContainerTypeServiceTest {
    private static final String CONTAINER_TYPE_1 = "Container Type 1";
    private static final String CONTAINER_TYPE_2 = "Container Type 2";
    private static final String UPDATED_CONTAINER_TYPE = "Updated Container Type";
    private static final String DESCRIPTION_1 = "Description 1";
    private static final String DESCRIPTION_2 = "Description 2";
    private static final String UPDATED_DESCRIPTION = "Updated Description";
    private static final String CONTAINER_SERIAL_NUMBER_1 = "Container Serial Number 1";
    private static final String CONTAINER_SERIAL_NUMBER_2 = "Container Serial Number 2";
    private static final BigDecimal CAPACITY_1 = BigDecimal.valueOf(2.0);
    private static final BigDecimal CAPACITY_2 = BigDecimal.valueOf(3.0);
    private static final BigDecimal UPDATED_BIGGER_CAPACITY = BigDecimal.valueOf(4.0);
    private static final BigDecimal UPDATED_SMALLER_CAPACITY = BigDecimal.valueOf(1.0);
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final String CONTAINER_TYPE_NOT_FOUND_WITH_ID_EXCEPTION = "Container type not found with id: %s";
    private static final String CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION =
            "Container type with name already exists: %s";
    private static final String CONTAINERS_EXCEED_NEW_CAPACITY_EXCEPTION =
            "Cannot update container type capacity. There are containers with occupied quantity exceeding new capacity. "
                    + "Container Serial Numbers: %s";
    private static final String CONTAINERS_STILL_HAVE_THIS_CONTAINER_TYPE_EXCEPTION =
            "Cannot delete container type with id %s because there are existing containers of this type";

    @Mock
    private ContainerTypeRepository containerTypeRepository;

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private Validator validator;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private ContainerTypeService containerTypeService;

    @Test
    void getAllContainerTypes_success() {
        // Arrange
        ContainerTypeEntity entity1 = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity entity2 = new ContainerTypeEntity(ID_2, CONTAINER_TYPE_2, DESCRIPTION_2, CAPACITY_2);
        List<ContainerTypeEntity> entities = List.of(entity1, entity2);
        when(containerTypeRepository.findAll()).thenReturn(entities);

        // Act
        List<ContainerTypeResponse> result = containerTypeService.getAllContainerTypes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ID_1, result.get(0).id());
        assertEquals(CONTAINER_TYPE_1, result.get(0).name());
        assertEquals(ID_2, result.get(1).id());
        assertEquals(CONTAINER_TYPE_2, result.get(1).name());
        verify(containerTypeRepository, times(1)).findAll();
    }

    @Test
    void getAllStorageLocations_emptyList() {
        // Arrange
        when(containerTypeRepository.findAll()).thenReturn(List.of());

        // Act
        List<ContainerTypeResponse> result = containerTypeService.getAllContainerTypes();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(containerTypeRepository, times(1)).findAll();
    }

    @Test
    void getContainerTypeById_success() {
        // Arrange
        ContainerTypeEntity entity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, null);
        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(entity));

        // Act
        ContainerTypeResponse result = containerTypeService.getContainerTypeById(ID_1);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(CONTAINER_TYPE_1, result.name());
        assertEquals(DESCRIPTION_1, result.description());
        verify(containerTypeRepository, times(1)).findById(ID_1);
    }

    @Test
    void getContainerTypeById_notFound() {
        // Arrange
        String message = CONTAINER_TYPE_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> containerTypeService.getContainerTypeById(ID_1));

        assertEquals(message, exception.getMessage());
        verify(containerTypeRepository, times(1)).findById(ID_1);
    }

    @Test
    void createContainerType_success() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity savedEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        String message = CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION.formatted(CONTAINER_TYPE_1);

        doNothing().when(validator).validateUniqueness(eq(CONTAINER_TYPE_1), any(), anyString());
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(savedEntity);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act
        ContainerTypeResponse result = containerTypeService.createContainerType(request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(CONTAINER_TYPE_1, result.name());
        assertEquals(DESCRIPTION_1, result.description());
        verify(validator, times(1)).validateUniqueness(eq(CONTAINER_TYPE_1), any(), anyString());
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void createContainerType_nameAlreadyExists() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        String errorMessage = CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION.formatted(CONTAINER_TYPE_1);

        doThrow(new IllegalArgumentException(errorMessage))
                .when(validator).validateUniqueness(eq(CONTAINER_TYPE_1), any(), eq(errorMessage));
        when(messageService.getMessage(any(), any())).thenReturn(errorMessage);

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> containerTypeService.createContainerType(request));

        assertEquals(errorMessage, exception.getMessage());
        verify(containerTypeRepository, never()).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateContainerType_success() {
        // Arrange
        ContainerTypeRequest request =
                new ContainerTypeRequest(UPDATED_CONTAINER_TYPE, UPDATED_DESCRIPTION, UPDATED_BIGGER_CAPACITY);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity updatedEntity =
                new ContainerTypeEntity(ID_1, UPDATED_CONTAINER_TYPE, UPDATED_DESCRIPTION, UPDATED_BIGGER_CAPACITY);
        String message = CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION.formatted(CONTAINER_TYPE_1);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(UPDATED_CONTAINER_TYPE)).thenReturn(false);
        when(validator.isNullOrEmpty(UPDATED_DESCRIPTION)).thenReturn(false);
        doNothing().when(validator).validateUniqueness(eq(UPDATED_CONTAINER_TYPE), any(), anyString());
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(updatedEntity);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act
        ContainerTypeResponse result = containerTypeService.updateContainerType(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_CONTAINER_TYPE, result.name());
        assertEquals(UPDATED_DESCRIPTION, result.description());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateStorageLocation_onlyName() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(UPDATED_CONTAINER_TYPE, null, null);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity updatedEntity =
                new ContainerTypeEntity(ID_1, UPDATED_CONTAINER_TYPE, DESCRIPTION_1, CAPACITY_1);
        String message = CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION.formatted(CONTAINER_TYPE_1);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(UPDATED_CONTAINER_TYPE)).thenReturn(false);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        doNothing().when(validator).validateUniqueness(eq(UPDATED_CONTAINER_TYPE), any(), anyString());
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(updatedEntity);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act
        ContainerTypeResponse result = containerTypeService.updateContainerType(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_CONTAINER_TYPE, result.name());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateStorageLocation_onlyDescription() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(null, UPDATED_DESCRIPTION, null);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity updatedEntity =
                new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, UPDATED_DESCRIPTION, CAPACITY_1);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isNullOrEmpty(UPDATED_DESCRIPTION)).thenReturn(false);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(updatedEntity);

        // Act
        ContainerTypeResponse result = containerTypeService.updateContainerType(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_DESCRIPTION, result.description());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateStorageLocation_onlyBiggerCapacity() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(null, null, UPDATED_BIGGER_CAPACITY);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity updatedEntity =
                new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, UPDATED_BIGGER_CAPACITY);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isPositiveBigDecimal(UPDATED_BIGGER_CAPACITY)).thenReturn(true);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(updatedEntity);

        // Act
        ContainerTypeResponse result = containerTypeService.updateContainerType(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_BIGGER_CAPACITY, result.capacity());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateStorageLocation_onlySmallerCapacity() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(null, null, UPDATED_SMALLER_CAPACITY);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeEntity updatedEntity =
                new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, UPDATED_SMALLER_CAPACITY);
        ContainerEntity entity1 = getContainerEntity(ID_1, CONTAINER_SERIAL_NUMBER_1);
        ContainerEntity entity2 = getContainerEntity(ID_2, CONTAINER_SERIAL_NUMBER_2);
        List<ContainerEntity> containerEntities = List.of(entity1, entity2);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isPositiveBigDecimal(UPDATED_SMALLER_CAPACITY)).thenReturn(true);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(containerTypeRepository.save(any(ContainerTypeEntity.class))).thenReturn(updatedEntity);
        when(containerRepository.findAllByContainerType_IdAndRemovedFalse(ID_1)).thenReturn(containerEntities);

        // Act
        ContainerTypeResponse result = containerTypeService.updateContainerType(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(ID_1, result.id());
        assertEquals(UPDATED_SMALLER_CAPACITY, result.capacity());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, times(1)).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateContainerType_notFound() {
        ContainerTypeRequest request =
                new ContainerTypeRequest(UPDATED_CONTAINER_TYPE, UPDATED_DESCRIPTION, UPDATED_BIGGER_CAPACITY);
        String message = CONTAINER_TYPE_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> containerTypeService.updateContainerType(ID_1, request));

        assertEquals(message, exception.getMessage());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, never()).save(any(ContainerTypeEntity.class));
    }

    @Test
    void updateContainerType_newCapacityExceededByContainers() {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(null, null, UPDATED_SMALLER_CAPACITY);
        ContainerTypeEntity existingEntity = new ContainerTypeEntity(ID_1, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerEntity entity1 = getContainerEntity(ID_1, CONTAINER_SERIAL_NUMBER_1);
        ContainerEntity entity2 = getContainerEntity(ID_2, CONTAINER_SERIAL_NUMBER_2);
        List<ContainerEntity> containerEntities = List.of(entity1, entity2);
        String message = CONTAINERS_EXCEED_NEW_CAPACITY_EXCEPTION;

        when(containerTypeRepository.findById(ID_1)).thenReturn(Optional.of(existingEntity));
        when(validator.isPositiveBigDecimal(UPDATED_SMALLER_CAPACITY)).thenReturn(true);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(containerRepository.findAllByContainerType_IdAndRemovedFalse(ID_1)).thenReturn(containerEntities);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> containerTypeService.updateContainerType(ID_1, request));

        assertEquals(message, exception.getMessage());
        verify(containerTypeRepository, times(1)).findById(ID_1);
        verify(containerTypeRepository, never()).save(any(ContainerTypeEntity.class));
    }

    @Test
    void deleteContainerTypeById_success() {
        // Arrange
        when(containerTypeRepository.existsById(ID_1)).thenReturn(true);
        when(containerRepository.existsByContainerType_Id(ID_1)).thenReturn(false);
        doNothing().when(containerTypeRepository).deleteById(ID_1);

        // Act
        containerTypeService.deleteContainerTypeById(ID_1);

        // Assert
        verify(containerTypeRepository, times(1)).existsById(ID_1);
        verify(containerRepository, times(1)).existsByContainerType_Id(ID_1);
        verify(containerTypeRepository, times(1)).deleteById(ID_1);
    }

    @Test
    void deleteContainerTypeById_notFound() {
        // Arrange
        String message = CONTAINER_TYPE_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);

        when(containerTypeRepository.existsById(ID_1)).thenReturn(false);
        when(messageService.getMessage(any(), any())).thenReturn(message);


        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> containerTypeService.deleteContainerTypeById(ID_1));

        // Assert
        assertEquals(message, exception.getMessage());
        verify(containerTypeRepository, times(1)).existsById(ID_1);
        verify(containerRepository, never()).existsByContainerType_Id(ID_1);
        verify(containerTypeRepository, never()).deleteById(ID_1);
    }

    @Test
    void deleteContainerTypeById_existContainerWithContainerType() {
        // Arrange
        String message = CONTAINERS_STILL_HAVE_THIS_CONTAINER_TYPE_EXCEPTION.formatted(ID_1);

        when(containerTypeRepository.existsById(ID_1)).thenReturn(true);
        when(containerRepository.existsByContainerType_Id(ID_1)).thenReturn(true);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> containerTypeService.deleteContainerTypeById(ID_1));

        // Assert
        assertEquals(message, exception.getMessage());
        verify(containerTypeRepository, times(1)).existsById(ID_1);
        verify(containerRepository, times(1)).existsByContainerType_Id(ID_1);
        verify(containerTypeRepository, never()).deleteById(ID_1);
    }

    private ContainerEntity getContainerEntity(final Long id, final String serialNumber) {
        return ContainerEntity.builder()
                .id(id)
                .serialNumber(serialNumber)
                .build();
    }
}
