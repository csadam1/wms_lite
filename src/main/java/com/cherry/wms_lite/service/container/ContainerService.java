package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import com.cherry.wms_lite.service.container_type.ContainerTypeValidationService;
import com.cherry.wms_lite.service.inventory.InventoryService;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerService {
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

    private final ContainerRepository containerRepository;
    private final ContainerTypeRepository containerTypeRepository;
    private final StorageLocationService storageLocationService;
    private final InventoryService inventoryService;
    private final ContainerTypeValidationService containerTypeValidationService;
    private final Validator validator;

    public List<ContainerResponse> getAllContainers() {
        return containerRepository.findAllByRemovedFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContainerResponse getContainerById(final Long containerId) {
        return mapToResponse(getContainerEntityById(containerId));
    }

    @Transactional
    public ContainerResponse createContainer(final ContainerRequest request) {
        validateCreateContainerRequest(request);

        ContainerTypeEntity containerType = getContainerTypeByName(request.containerTypeName());
        InventoryEntity attachedToInventoryEntity =
                getAttachedInventory(request.locationName(), request.locationTypeEnum());

        ContainerEntity containerEntity = ContainerEntity.builder()
                .serialNumber(request.serialNumber())
                .containerType(containerType)
                .inventoryEntity(inventoryService.createNewInventory())
                .attachedToInventoryEntity(attachedToInventoryEntity)
                .createdAt(Instant.now())
                .status(request.status())
                .removed(false)
                .build();

        return mapToResponse(containerRepository.save(containerEntity));
    }

    @Transactional
    public ContainerResponse updateContainer(final Long containerId, final ContainerRequest request) {
        ContainerEntity containerEntity = getContainerEntityById(containerId);

        validateIsSerialNumberAlreadyExist(request.serialNumber());

        changeStatusIfProvided(containerEntity, request);
        changeContainerTypeIfProvided(containerEntity, request);
        changeLocationIfProvided(containerEntity, request);

        return mapToResponse(containerRepository.save(containerEntity));
    }

    @Transactional
    public void removeContainerById(final Long containerId) {
        if (!isContainerInventoryEmpty(containerId)) {
            throw new IllegalStateException(CONTAINER_NOT_EMPTY_EXCEPTION.formatted(containerId));
        }

        ContainerEntity containerEntity = getContainerEntityById(containerId);
        containerEntity.setRemoved(true);
        containerRepository.save(containerEntity);
    }

    private ContainerTypeEntity getContainerTypeByName(final String name) {
        return containerTypeRepository.findByName(name)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                CONTAINER_TYPE_NOT_FOUND_WITH_NAME_EXCEPTION.formatted(name)));
    }

    private void validateIsSerialNumberAlreadyExist(final String serialNumber) {
        if (!validator.isNullOrEmpty(serialNumber)) {
            validator.validateUniqueness(serialNumber, containerRepository::findBySerialNumberAndRemovedFalse,
                    CONTAINER_WITH_SERIAL_EXIST_EXCEPTION.formatted(serialNumber));
        }
    }

    private void changeStatusIfProvided(final ContainerEntity containerEntity, final ContainerRequest request) {
        if (!validator.isNullOrEmpty(request.status())) {
            containerEntity.setStatus(request.status());
        }
    }

    private void changeContainerTypeIfProvided(final ContainerEntity containerEntity, final ContainerRequest request) {
        if (!validator.isNullOrEmpty(request.containerTypeName())) {
            ContainerTypeEntity containerType = getContainerTypeByName(request.containerTypeName());

            validateContainerTypeChange(containerEntity, containerType);
            containerEntity.setContainerType(containerType);
        }
    }

    private void validateContainerTypeChange(final ContainerEntity containerEntity,
                                             final ContainerTypeEntity containerType)
    {
        boolean isValid =
                containerTypeValidationService.isContainerTypeChangeValid(containerEntity, containerType.getCapacity());
        if (!isValid) {
            throw new IllegalStateException(
                    PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(containerEntity.getSerialNumber()));
        }
    }

    private void changeLocationIfProvided(final ContainerEntity containerEntity, final ContainerRequest request) {
        if (!validator.isNullOrEmpty(request.locationName()) && !validator.isNullOrEmpty(request.locationTypeEnum())) {

            containerEntity.setAttachedToInventoryEntity(
                    getContainerAttachedToInventory(request.locationName(), request.locationTypeEnum()));

            validateContainerLocationChange(containerEntity, request);
        }
    }

    private void validateContainerLocationChange(final ContainerEntity containerEntity,
                                                 final ContainerRequest request)
    {
        if (request.locationTypeEnum().equals(LocationTypeEnum.STORAGE_LOCATION)) {
            return;
        }

        BigDecimal capacity = containerEntity.getContainerType().getCapacity();
        boolean isValid = containerTypeValidationService.containerFitsIntoLocation(containerEntity, capacity);
        if (!isValid) {
            throw new IllegalStateException(
                    PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(containerEntity.getSerialNumber()));
        }
    }

    private void validateCreateContainerRequest(final ContainerRequest request) {
        //Is Container already exist
        validator.validateUniqueness(request.serialNumber(), containerRepository::findBySerialNumberAndRemovedFalse,
                CONTAINER_WITH_SERIAL_EXIST_EXCEPTION.formatted(request.serialNumber())
        );

        //Does target location exist
        if (request.locationTypeEnum().equals(LocationTypeEnum.STORAGE_LOCATION)) {
            storageLocationService.getStorageLocationByName(request.locationName());
        } else {
            // Does parent container have enough capacity for new container
            BigDecimal capacity = getContainerTypeByName(request.containerTypeName()).getCapacity();
            ContainerEntity parentContainer = getContainerEntityByName(request.locationName());
            boolean isContainerOverloaded =
                    !containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(parentContainer, capacity);

            if (isContainerOverloaded) {
                throw new IllegalStateException(
                        PARENT_CONTAINER_CAPACITY_EXCEEDED_EXCEPTION.formatted(parentContainer.getSerialNumber()));
            }
        }
    }

    private boolean isContainerInventoryEmpty(final Long containerId) {
        InventoryEntity containerInventory = getContainerInventoryById(containerId);

        return containerInventory.getItems().isEmpty()
                && containerInventory.getContainers().isEmpty();
    }

    private InventoryEntity getAttachedInventory(String locationName, LocationTypeEnum locationType) {
        return locationType == LocationTypeEnum.CONTAINER
                ? getContainerInventory(locationName)
                : storageLocationService.getStorageLocationInventoryByName(locationName);
    }

    private InventoryEntity getContainerInventory(final String serialNumber) {
        return getContainerEntityByName(serialNumber)
                .getInventoryEntity();
    }

    private InventoryEntity getContainerInventoryById(final Long containerId) {
        return getContainerEntityById(containerId)
                .getInventoryEntity();
    }

    private InventoryEntity getContainerAttachedToInventory(final String locationName,
                                                            final LocationTypeEnum locationTypeEnum)
    {
        return locationTypeEnum.equals(LocationTypeEnum.CONTAINER)
                ? getContainerInventory(locationName)
                : storageLocationService.getStorageLocationInventoryByName(locationName);
    }

    private ContainerResponse mapToResponse(final ContainerEntity container) {
        return new ContainerResponse(
                container.getId(),
                container.getContainerType().getName(),
                container.getSerialNumber(),
                container.getCreatedAt().truncatedTo(ChronoUnit.MILLIS),
                container.getStatus(),
                getLocationName(container)
        );
    }

    private String getLocationName(final ContainerEntity container) {
        return container.getAttachedToInventoryEntity().getStorageLocation() != null
                ? container.getAttachedToInventoryEntity().getStorageLocation().getName()
                : container.getAttachedToInventoryEntity().getContainer().getSerialNumber();
    }

    private ContainerEntity getContainerEntityById(final Long id) {
        return containerRepository.findByIdAndRemovedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException(CONTAINER_NOT_FOUND_WITH_ID_EXCEPTION.formatted(id)));
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumberAndRemovedFalse(serialNumber)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                CONTAINER_NOT_FOUND_WITH_SERIAL_NUMBER_EXCEPTION.formatted(serialNumber)));
    }
}
