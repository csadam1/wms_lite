package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.mapper.container.ContainerMapper;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import com.cherry.wms_lite.service.inventory.InventoryService;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerService {
    private final ContainerRepository containerRepository;
    private final ContainerTypeRepository containerTypeRepository;
    private final StorageLocationService storageLocationService;
    private final InventoryService inventoryService;
    private final Validator validator;
    private final MessageService messageService;
    private final ContainerValidationService containerValidationService;
    private final ContainerMapper containerMapper;

    public List<ContainerResponse> getAllContainers() {
        return containerRepository.findAllByRemovedFalse().stream()
                .map(containerMapper::toResponse)
                .toList();
    }

    public ContainerResponse getContainerById(final Long containerId) {
        return containerMapper.toResponse(getContainerEntityById(containerId));
    }

    @Transactional
    public ContainerResponse createContainer(final ContainerRequest request) {
        validateSerialNumberUniqueness(request.serialNumber());

        ContainerTypeEntity containerType = getContainerTypeByName(request.containerTypeName());
        InventoryEntity attachedToInventoryEntity =
                getAttachedInventory(request.locationName(), request.locationTypeEnum());

        ContainerEntity container = ContainerEntity.builder()
                .serialNumber(request.serialNumber())
                .containerType(containerType)
                .inventoryEntity(inventoryService.createNewInventory())
                .attachedToInventoryEntity(attachedToInventoryEntity)
                .createdAt(Instant.now())
                .status(request.status())
                .removed(false)
                .build();

        containerValidationService.validateIsContainerFitIntoInventory(container);
        return containerMapper.toResponse(containerRepository.save(container));
    }

    @Transactional
    public ContainerResponse updateContainer(final Long containerId, final ContainerRequest request) {
        ContainerEntity container = getContainerEntityById(containerId);

        changeSerialNumberIfProvided(container, request);
        changeStatusIfProvided(container, request);
        changeContainerTypeIfProvided(container, request);
        changeLocationIfProvided(container, request);

        containerValidationService.validateIsContainerFitIntoInventory(container);
        containerValidationService.validateIsContentFitIntoContainerInventory(container);

        return containerMapper.toResponse(containerRepository.save(container));
    }

    @Transactional
    public void removeContainerById(final Long containerId) {
        if (!isContainerInventoryEmpty(containerId)) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_NOT_EMPTY, containerId));
        }

        ContainerEntity containerEntity = getContainerEntityById(containerId);
        containerEntity.setRemoved(true);
        containerRepository.save(containerEntity);
    }

    private ContainerTypeEntity getContainerTypeByName(final String name) {
        return containerTypeRepository.findByName(name)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_NAME, name)));
    }

    private void changeSerialNumberIfProvided(final ContainerEntity container, final ContainerRequest request) {
        if (!validator.isNullOrEmpty(request.serialNumber())) {
            validateSerialNumberUniqueness(request.serialNumber());
            container.setSerialNumber(request.serialNumber());
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
            containerEntity.setContainerType(containerType);
        }
    }

    private void changeLocationIfProvided(final ContainerEntity containerEntity, final ContainerRequest request) {
        if (!validator.isNullOrEmpty(request.locationName()) && !validator.isNullOrEmpty(request.locationTypeEnum())) {
            containerEntity.setAttachedToInventoryEntity(
                    getContainerAttachedToInventory(request.locationName(), request.locationTypeEnum()));
        }
    }

    private void validateSerialNumberUniqueness(final String serialNumber) {
        validator.validateUniqueness(serialNumber, containerRepository::findBySerialNumberAndRemovedFalse,
                messageService.getMessage(ExceptionMessageKeys.CONTAINER_SERIAL_EXISTS, serialNumber));
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

    private ContainerEntity getContainerEntityById(final Long id) {
        return containerRepository.findByIdAndRemovedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.CONTAINER_NOT_FOUND_WITH_ID, id)));
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumberAndRemovedFalse(serialNumber)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.CONTAINER_NOT_FOUND_WITH_SERIAL, serialNumber)));
    }
}
