package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.*;
import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import com.cherry.wms_lite.service.container_type.ContainerTypeValidationService;
import com.cherry.wms_lite.service.inventory.InventoryService;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {
    private static final String CONTAINER_WITH_SERIAL_EXIST_EXCEPTION =
            "Container with serial number already exists: %s";
    private static final String CONTAINER_NOT_FOUND_WITH_ID_EXCEPTION = "Container not found with id: %s";
    private static final String CONTAINER_NOT_FOUND_WITH_SERIAL_NUMBER_EXCEPTION =
            "Container not found with serial number: %s";
    private static final String CONTAINER_NOT_EMPTY_EXCEPTION =
            "Cannot remove container with non-empty inventory. Container id: %s";
    private static final String PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION =
            "Cannot create container. Parent container capacity exceeded. Container Serial Number: %s";
    private static final String CONTAINER_TYPE_NOT_FOUND_WITH_NAME_EXCEPTION = "Container type not found with name: %s";

    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final Long ID_3 = 3L;
    private static final String SERIAL_NUMBER_1 = "SN-001";
    private static final String SERIAL_NUMBER_2 = "SN-002";
    private static final String SERIAL_NUMBER_3 = "SN-003";
    private static final String CONTAINER_TYPE_NAME_1 = "Type A";
    private static final String CONTAINER_TYPE_NAME_2 = "Type B";
    private static final String LOCATION_NAME = "Loc-1";
    private static final String LOCATION_NAME_2 = "Loc-2";
    private static final BigDecimal CAPACITY_1 = BigDecimal.valueOf(9.0);
    private static final BigDecimal CAPACITY_2 = BigDecimal.valueOf(10.0);

    @Mock
    private ContainerRepository containerRepository;
    @Mock
    private ContainerTypeRepository containerTypeRepository;
    @Mock
    private StorageLocationService storageLocationService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ContainerTypeValidationService containerTypeValidationService;
    @Mock
    private Validator validator;
    @Mock
    private MessageService messageService;
    @InjectMocks
    private ContainerService containerService;

    @Test
    void getAllContainers_returnsListOfResponses() {
        // Arrange
        List<ContainerEntity> containers = List.of(buildContainer1(), buildContainer2());

        when(containerRepository.findAllByRemovedFalse()).thenReturn(containers);

        // Act
        List<ContainerResponse> result = containerService.getAllContainers();

        // Assert
        assertEquals(2, result.size());

        ContainerResponse elem1 = result.getFirst();
        assertEquals(SERIAL_NUMBER_1, elem1.containerSerialNumber());
        assertEquals(CONTAINER_TYPE_NAME_1, elem1.containerType());
        assertEquals(Instant.EPOCH, elem1.createdAt());
        assertEquals(ContainerStatusEnum.OPEN, elem1.status());
        assertEquals(LOCATION_NAME, elem1.locationName());

        ContainerResponse elem2 = result.get(1);
        assertEquals(SERIAL_NUMBER_2, elem2.containerSerialNumber());
        assertEquals(CONTAINER_TYPE_NAME_2, elem2.containerType());
        assertEquals(Instant.EPOCH, elem2.createdAt());
        assertEquals(ContainerStatusEnum.CLOSED, elem2.status());
        assertEquals(SERIAL_NUMBER_3, elem2.locationName());

        verify(containerRepository).findAllByRemovedFalse();
    }

    @Test
    void getAllContainers_returnsEmptyList() {
        // Arrange
        when(containerRepository.findAllByRemovedFalse()).thenReturn(List.of());

        // Act
        List<ContainerResponse> result = containerService.getAllContainers();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getContainerById_found_returnsResponse() {
        // Arrange
        ContainerEntity container = buildContainer1();

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));

        // Act
        ContainerResponse result = containerService.getContainerById(ID_1);

        // Assert
        assertEquals(SERIAL_NUMBER_1, result.containerSerialNumber());
        assertEquals(CONTAINER_TYPE_NAME_1, result.containerType());
        assertEquals(Instant.EPOCH, result.createdAt());
        assertEquals(ContainerStatusEnum.OPEN, result.status());
        assertEquals(LOCATION_NAME, result.locationName());
    }

    @Test
    void getContainerById_notFound_throwsEntityNotFoundException() {
        // Arrange
        String message = CONTAINER_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);
        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> containerService.getContainerById(ID_1));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void createContainer_storageLocation_success() {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_NAME_1, ContainerStatusEnum.OPEN, LOCATION_NAME,
                        LocationTypeEnum.STORAGE_LOCATION);

        InventoryEntity newInventory = buildEmptyInventory();
        InventoryEntity attachedInventory = buildStorageLocationInventory();
        ContainerTypeEntity containerType = buildContainerType1();
        ContainerEntity savedContainer = buildContainer1WithEmptyInventory();

        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_1)).thenReturn(Optional.of(containerType));
        when(storageLocationService.getStorageLocationInventoryByName(LOCATION_NAME)).thenReturn(attachedInventory);
        when(inventoryService.createNewInventory()).thenReturn(newInventory);
        when(containerRepository.save(any())).thenReturn(savedContainer);
        doNothing().when(validator).validateUniqueness(eq(SERIAL_NUMBER_1), any(), any());
        when(storageLocationService.getStorageLocationByName(LOCATION_NAME))
                .thenReturn(buildStorageLocation());

        // Act
        ContainerResponse result = containerService.createContainer(request);

        // Assert
        assertEquals(ID_1, result.id());
        assertEquals(SERIAL_NUMBER_1, result.containerSerialNumber());
        assertEquals(CONTAINER_TYPE_NAME_1, result.containerType());
        assertEquals(Instant.EPOCH, result.createdAt());
        assertEquals(ContainerStatusEnum.OPEN, result.status());
        assertEquals(LOCATION_NAME, result.locationName());

        verify(containerRepository).save(any());
    }

    @Test
    void createContainer_containerLocation_success() {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_NAME_1, ContainerStatusEnum.OPEN, SERIAL_NUMBER_3,
                        LocationTypeEnum.CONTAINER);

        InventoryEntity newInventory = buildEmptyInventory();
        InventoryEntity attachedInventory = buildContainerInventory();
        ContainerEntity parentContainer = attachedInventory.getContainer();
        ContainerTypeEntity containerType = buildContainerType1();
        ContainerEntity container = buildContainerWithContainerInventory(attachedInventory);

        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_1)).thenReturn(Optional.of(containerType));
        when(containerRepository.findBySerialNumberAndRemovedFalse(SERIAL_NUMBER_3)).thenReturn(
                Optional.of(parentContainer));
        when(containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(parentContainer, CAPACITY_1))
                .thenReturn(true);
        when(inventoryService.createNewInventory()).thenReturn(newInventory);
        when(containerRepository.save(any())).thenReturn(container);
        doNothing().when(validator).validateUniqueness(eq(SERIAL_NUMBER_1), any(), any());

        // Act
        ContainerResponse result = containerService.createContainer(request);

        // Assert
        assertNotNull(result);

        assertEquals(ID_1, result.id());
        assertEquals(SERIAL_NUMBER_1, result.containerSerialNumber());
        assertEquals(CONTAINER_TYPE_NAME_1, result.containerType());
        assertEquals(Instant.EPOCH, result.createdAt());
        assertEquals(ContainerStatusEnum.OPEN, result.status());
        assertEquals(SERIAL_NUMBER_3, result.locationName());

        verify(containerRepository).save(any());
    }

    @Test
    void createContainer_containerLocation_parentCapacityExceeded_throwsIllegalStateException() {
        // Assert
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_NAME_2, ContainerStatusEnum.OPEN, SERIAL_NUMBER_3,
                        LocationTypeEnum.CONTAINER);

        ContainerTypeEntity containerType = buildContainerType2();
        ContainerEntity parentContainer = buildParentContainer();
        String message = PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(SERIAL_NUMBER_3);

        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_2)).thenReturn(Optional.of(containerType));
        when(containerRepository.findBySerialNumberAndRemovedFalse(SERIAL_NUMBER_3)).thenReturn(
                Optional.of(parentContainer));
        when(containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(parentContainer, CAPACITY_2))
                .thenReturn(false);
        doNothing().when(validator).validateUniqueness(eq(SERIAL_NUMBER_1), any(), any());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> containerService.createContainer(request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void createContainer_containerLocation_parentContainerNotFound_throwsEntityNotFoundException() {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_NAME_1, ContainerStatusEnum.OPEN, SERIAL_NUMBER_3,
                        LocationTypeEnum.CONTAINER);

        ContainerTypeEntity containerType = buildContainerType1();
        String message = CONTAINER_NOT_FOUND_WITH_SERIAL_NUMBER_EXCEPTION.formatted(SERIAL_NUMBER_3);

        doNothing().when(validator).validateUniqueness(eq(SERIAL_NUMBER_1), any(), any());
        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_1)).thenReturn(Optional.of(containerType));
        when(containerRepository.findBySerialNumberAndRemovedFalse(SERIAL_NUMBER_3)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> containerService.createContainer(request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void createContainer_containerTypeNotFound_throwsEntityNotFoundException() {
        ContainerRequest request = new ContainerRequest(
                SERIAL_NUMBER_1, CONTAINER_TYPE_NAME_1, ContainerStatusEnum.OPEN, LOCATION_NAME,
                LocationTypeEnum.STORAGE_LOCATION);
        String message = CONTAINER_TYPE_NOT_FOUND_WITH_NAME_EXCEPTION.formatted(CONTAINER_TYPE_NAME_1);

        doNothing().when(validator).validateUniqueness(eq(SERIAL_NUMBER_1), any(), any());
        when(storageLocationService.getStorageLocationByName(LOCATION_NAME))
                .thenReturn(buildStorageLocation());
        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_1)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> containerService.createContainer(request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void updateContainer_serialNumberDuplicated_throwsContainerWithSerialNumberExist() {
        // Arrange
        ContainerRequest request = new ContainerRequest(
                SERIAL_NUMBER_2, null, null, null, null);

        String message = CONTAINER_WITH_SERIAL_EXIST_EXCEPTION.formatted(SERIAL_NUMBER_2);
        ContainerEntity container = buildContainer1WithEmptyInventory();
        IllegalArgumentException exception = new IllegalArgumentException(message);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(validator.isNullOrEmpty(SERIAL_NUMBER_2)).thenReturn(false);
        doThrow(exception).when(validator).validateUniqueness(eq(SERIAL_NUMBER_2), any(), any());

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> containerService.updateContainer(ID_1, request));

        // Assert
        assertEquals(message, ex.getMessage());
    }

    @Test
    void updateContainer_changeStatus_success() {
        // Arrange
        ContainerRequest request = new ContainerRequest(
                null, null, ContainerStatusEnum.CLOSED, null, null);

        ContainerEntity openContainer = buildContainer1WithEmptyInventory();
        ContainerEntity closedContainer = buildContainer1WithEmptyInventory();
        closedContainer.setStatus(ContainerStatusEnum.CLOSED);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(openContainer));
        when(containerRepository.save(closedContainer)).thenReturn(closedContainer);
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(ContainerStatusEnum.CLOSED)).thenReturn(false);

        // Act
        ContainerResponse result = containerService.updateContainer(ID_1, request);

        // Assert
        assertEquals(ContainerStatusEnum.CLOSED, result.status());
        assertNotNull(result);
    }

    @Test
    void updateContainer_changeContainerType_valid_success() {
        // Arrange
        ContainerRequest request = new ContainerRequest(null, CONTAINER_TYPE_NAME_2, null, null, null);

        ContainerEntity container = buildContainer1WithEmptyInventory();
        ContainerTypeEntity newContainerType = buildContainerType2();

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(CONTAINER_TYPE_NAME_2)).thenReturn(false);
        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_2)).thenReturn(Optional.of(newContainerType));
        when(containerTypeValidationService.isContainerTypeChangeValid(container, CAPACITY_2)).thenReturn(true);
        when(containerRepository.save(container)).thenReturn(container);

        // Act
        ContainerResponse result = containerService.updateContainer(ID_1, request);

        // Assert
        assertEquals(newContainerType, container.getContainerType());
        assertNotNull(result);
    }

    @Test
    void updateContainer_changeContainerType_capacityExceeded_throwsIllegalStateException() {
        // Arrange
        ContainerRequest request = new ContainerRequest(null, CONTAINER_TYPE_NAME_2, null, null, null);

        ContainerEntity container = buildContainer1WithEmptyInventory();
        ContainerTypeEntity newContainerType = buildContainerType2();
        String message = PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(SERIAL_NUMBER_1);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(CONTAINER_TYPE_NAME_2)).thenReturn(false);
        when(containerTypeRepository.findByName(CONTAINER_TYPE_NAME_2)).thenReturn(Optional.of(newContainerType));
        when(containerTypeValidationService.isContainerTypeChangeValid(container, CAPACITY_2)).thenReturn(false);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> containerService.updateContainer(ID_1, request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void updateContainer_changeLocation_toStorageLocation_success() {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(null, null, null, LOCATION_NAME_2, LocationTypeEnum.STORAGE_LOCATION);

        ContainerEntity oldContainer = buildContainer1WithEmptyInventory();
        InventoryEntity newStorageLocation = buildStorageLocationInventory2();
        ContainerEntity newContainer = buildContainer1WithEmptyInventory();
        newContainer.setAttachedToInventoryEntity(newStorageLocation);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(oldContainer));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(LOCATION_NAME_2)).thenReturn(false);
        when(validator.isNullOrEmpty(LocationTypeEnum.STORAGE_LOCATION)).thenReturn(false);
        when(storageLocationService.getStorageLocationInventoryByName(LOCATION_NAME_2)).thenReturn(newStorageLocation);
        when(containerRepository.save(newContainer)).thenReturn(newContainer);

        // Act
        ContainerResponse result = containerService.updateContainer(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(LOCATION_NAME_2, result.locationName());
    }

    @Test
    void updateContainer_changeLocation_toContainer_fits_success() {
        // Arrange
        ContainerRequest request = new ContainerRequest(null, null, null, SERIAL_NUMBER_2, LocationTypeEnum.CONTAINER);

        ContainerEntity parentContainer = buildContainer2WithEmptyInventory();
        parentContainer.getInventoryEntity().setContainer(parentContainer);
        ContainerEntity container = buildContainer1WithEmptyInventory();

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(SERIAL_NUMBER_2)).thenReturn(false);
        when(validator.isNullOrEmpty(LocationTypeEnum.CONTAINER)).thenReturn(false);
        when(containerRepository.findBySerialNumberAndRemovedFalse(SERIAL_NUMBER_2)).thenReturn(
                Optional.of(parentContainer));
        when(containerTypeValidationService.containerFitsIntoLocation(container, CAPACITY_1)).thenReturn(true);
        when(containerRepository.save(container)).thenReturn(container);

        // Act
        ContainerResponse result = containerService.updateContainer(ID_1, request);

        // Assert
        assertNotNull(result);
        assertEquals(SERIAL_NUMBER_2, result.locationName());
    }

    @Test
    void updateContainer_changeLocation_toContainer_doesNotFit_throwsIllegalStateException() {
        // Arrange
        ContainerRequest request = new ContainerRequest(null, null, null, SERIAL_NUMBER_2, LocationTypeEnum.CONTAINER);

        ContainerEntity parentContainer = buildContainer2WithEmptyInventory();
        parentContainer.getInventoryEntity().setContainer(parentContainer);
        ContainerEntity container = buildContainer1WithEmptyInventory();
        container.setAttachedToInventoryEntity(parentContainer.getInventoryEntity());
        String message = PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(SERIAL_NUMBER_1);
        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(validator.isNullOrEmpty(null)).thenReturn(true);
        when(validator.isNullOrEmpty(SERIAL_NUMBER_2)).thenReturn(false);
        when(validator.isNullOrEmpty(LocationTypeEnum.CONTAINER)).thenReturn(false);
        when(containerRepository.findBySerialNumberAndRemovedFalse(SERIAL_NUMBER_2)).thenReturn(
                Optional.of(parentContainer));
        when(containerTypeValidationService.containerFitsIntoLocation(container, CAPACITY_1)).thenReturn(false);
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> containerService.updateContainer(ID_1, request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void updateContainer_containerNotFound_throwsEntityNotFoundException() {
        // Arrange
        ContainerRequest request = new ContainerRequest(null, null, ContainerStatusEnum.OPEN, null, null);
        String message = CONTAINER_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> containerService.updateContainer(ID_1, request));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void removeContainerById_emptyInventory_success() {
        // Arrange
        ContainerEntity container = buildContainer1WithEmptyInventory();
        container.setRemoved(true);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(containerRepository.save(container)).thenReturn(container);

        // Act
        containerService.removeContainerById(ID_1);

        // Assert
        assertTrue(container.getRemoved());
        verify(containerRepository).save(container);
    }

    @Test
    void removeContainerById_nonEmptyInventory_hasItems_throwsIllegalStateException() {
        // Arrange
        List<ItemEntity> items = List.of(ItemEntity.builder().id(ID_1).quantity(BigDecimal.ONE).build());
        InventoryEntity inventoryWithItems = InventoryEntity.builder().id(ID_1).items(items).build();
        InventoryEntity attachedInventory = buildStorageLocationInventory();

        ContainerEntity container = ContainerEntity.builder()
                .id(ID_1)
                .serialNumber(SERIAL_NUMBER_1)
                .containerType(buildContainerType1())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .inventoryEntity(inventoryWithItems)
                .attachedToInventoryEntity(attachedInventory)
                .removed(true)
                .build();

        String message = CONTAINER_NOT_EMPTY_EXCEPTION.formatted(ID_1);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> containerService.removeContainerById(ID_1));

        assertEquals(message, ex.getMessage());
        verify(containerRepository, never()).save(any());
    }

    @Test
    void removeContainerById_nonEmptyInventory_hasContainers_throwsIllegalStateException() {
        // Arrange
        ContainerEntity childContainer = buildContainer1WithEmptyInventory();
        List<ContainerEntity> containers = List.of(childContainer);
        InventoryEntity inventoryWithContainers = InventoryEntity.builder().id(ID_1).containers(containers).build();
        InventoryEntity attachedInventory = buildStorageLocationInventory();
        ContainerEntity container = ContainerEntity.builder()
                .id(ID_1)
                .serialNumber(SERIAL_NUMBER_1)
                .containerType(buildContainerType1())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .inventoryEntity(inventoryWithContainers)
                .attachedToInventoryEntity(attachedInventory)
                .build();

        String message = CONTAINER_NOT_EMPTY_EXCEPTION.formatted(ID_1);

        when(containerRepository.findByIdAndRemovedFalse(ID_1)).thenReturn(Optional.of(container));
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> containerService.removeContainerById(ID_1));

        assertEquals(message, ex.getMessage());
    }

    @Test
    void removeContainerById_containerNotFound_throwsEntityNotFoundException() {
        // Arrange
        String message = CONTAINER_NOT_FOUND_WITH_ID_EXCEPTION.formatted(ID_1);

        when(containerRepository.findByIdAndRemovedFalse(ID_1))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn(message);

        // Act and Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> containerService.removeContainerById(ID_1));

        assertEquals(message, ex.getMessage());
    }

    private ContainerTypeEntity buildContainerType1() {
        return ContainerTypeEntity.builder()
                .id(ID_1).name(CONTAINER_TYPE_NAME_1).capacity(CAPACITY_1).build();
    }

    private ContainerTypeEntity buildContainerType2() {
        return ContainerTypeEntity.builder()
                .id(ID_2).name(CONTAINER_TYPE_NAME_2).capacity(CAPACITY_2).build();
    }

    private InventoryEntity buildEmptyInventory() {
        return InventoryEntity.builder().id(ID_1).build();
    }

    private StorageLocationEntity buildStorageLocation() {
        return StorageLocationEntity.builder().id(ID_1).name(LOCATION_NAME).build();
    }

    private InventoryEntity buildStorageLocationInventory() {
        StorageLocationEntity sl = buildStorageLocation();
        InventoryEntity inv = InventoryEntity.builder().id(ID_2).storageLocation(sl).build();
        sl.setInventoryEntity(inv);
        return inv;
    }

    private StorageLocationEntity buildStorageLocation2() {
        return StorageLocationEntity.builder().id(ID_1).name(LOCATION_NAME_2).build();
    }

    private InventoryEntity buildStorageLocationInventory2() {
        StorageLocationEntity sl = buildStorageLocation2();
        InventoryEntity inv = InventoryEntity.builder().id(ID_3).storageLocation(sl).build();
        sl.setInventoryEntity(inv);
        return inv;
    }

    private ContainerEntity buildParentContainer() {
        return ContainerEntity.builder().id(ID_3).serialNumber(SERIAL_NUMBER_3).build();
    }

    private InventoryEntity buildContainerInventory() {
        ContainerEntity parentContainer = buildParentContainer();
        InventoryEntity inv = InventoryEntity.builder().id(ID_3).container(parentContainer).build();
        parentContainer.setInventoryEntity(inv);
        return inv;
    }

    private ContainerEntity buildContainer2() {
        return ContainerEntity.builder()
                .id(ID_2)
                .serialNumber(SERIAL_NUMBER_2)
                .containerType(buildContainerType2())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.CLOSED)
                .attachedToInventoryEntity(buildContainerInventory())
                .build();
    }

    private ContainerEntity buildContainer1() {
        return ContainerEntity.builder()
                .id(ID_1)
                .serialNumber(SERIAL_NUMBER_1)
                .containerType(buildContainerType1())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .attachedToInventoryEntity(buildStorageLocationInventory())
                .build();
    }

    private ContainerEntity buildContainer2WithEmptyInventory() {
        return ContainerEntity.builder()
                .id(ID_2)
                .serialNumber(SERIAL_NUMBER_2)
                .containerType(buildContainerType2())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .inventoryEntity(buildEmptyInventory())
                .attachedToInventoryEntity(buildContainerInventory())
                .build();
    }

    private ContainerEntity buildContainer1WithEmptyInventory() {
        return ContainerEntity.builder()
                .id(ID_1)
                .serialNumber(SERIAL_NUMBER_1)
                .containerType(buildContainerType1())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .inventoryEntity(buildEmptyInventory())
                .attachedToInventoryEntity(buildStorageLocationInventory())
                .build();
    }

    private ContainerEntity buildContainerWithContainerInventory(final InventoryEntity attachedToInventory) {
        return ContainerEntity.builder()
                .id(ID_1)
                .serialNumber(SERIAL_NUMBER_1)
                .containerType(buildContainerType1())
                .createdAt(Instant.EPOCH)
                .status(ContainerStatusEnum.OPEN)
                .inventoryEntity(buildEmptyInventory())
                .attachedToInventoryEntity(attachedToInventory)
                .build();
    }
}
