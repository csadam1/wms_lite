package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.service.container_type.ContainerTypeService;
import com.cherry.wms_lite.service.inventory.InventoryService;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerService {
    private final ContainerRepository containerRepository;
    private final StorageLocationService storageLocationService;
    private final InventoryService inventoryService;
    private final ContainerTypeService containerTypeService;

    public List<ContainerResponse> getAllContainers() {
        return containerRepository.findAll().stream()
                .filter(containerEntity -> !containerEntity.getRemoved())
                .map(this::mapToResponse)
                .toList();
    }

    public ContainerResponse getContainerById(final Long containerId) {
        return mapToResponse(getContainerEntityById(containerId));
    }

    @Transactional
    public ContainerResponse createContainer(final ContainerRequest request) {
        validateSerialNumberUniqueness(request.serialNumber());

        ContainerTypeEntity containerType = containerTypeService.getContainerTypeByName(request.containerTypeName());
        InventoryEntity attachedToInventoryEntity =
                getAttachedInventory(request.locationName(), request.locationTypeEnum());

        ContainerEntity containerEntity = ContainerEntity.builder()
                .serialNumber(request.serialNumber())
                .containerType(containerType)
                .inventoryEntity(inventoryService.getNewInventory())
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
        // Update serial number if provided
        if (isValid(request.serialNumber())) {
            validateSerialNumberUniqueness(request.serialNumber());
            containerEntity.setSerialNumber(request.serialNumber());
        }

        // Update status if provided
        if (request.status() != null) {
            containerEntity.setStatus(request.status());
        }

        // Update container type if provided
        if (isValid(request.containerTypeName())) {
            ContainerTypeEntity containerType =
                    containerTypeService.getContainerTypeByName(request.containerTypeName());
            containerEntity.setContainerType(containerType);
        }

        // Update location if provided
        if (isValid(request.locationName()) && request.locationTypeEnum() != null) {
            containerEntity.setAttachedToInventoryEntity(
                    getContainerAttachedToInventory(request.locationName(), request.locationTypeEnum()));
        }

        return mapToResponse(containerRepository.save(containerEntity));
    }

    @Transactional
    public void removeContainerById(final Long containerId) {
        ContainerEntity containerEntity = getContainerEntityById(containerId);
        containerEntity.setRemoved(true);
        containerRepository.save(containerEntity);
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    private InventoryEntity getAttachedInventory(String locationName, LocationTypeEnum locationType) {
        return locationType == LocationTypeEnum.CONTAINER
                ? getContainerInventory(locationName)
                : storageLocationService.getStorageLocationInventoryByName(locationName);
    }

    private void validateSerialNumberUniqueness(final String serialNumber) {
        containerRepository.findBySerialNumber(serialNumber)
                .ifPresent(container -> {
                    throw new IllegalArgumentException("Container with serial number already exists: " + serialNumber);
                });
    }

    private InventoryEntity getContainerInventory(final String serialNumber) {
        return getContainerEntityByName(serialNumber)
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
                container.getStatus()
        );
    }

    private ContainerEntity getContainerEntityById(final Long id) {
        return containerRepository.findById(id)
                .filter(containerEntity -> !containerEntity.getRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Container not found with id: " + id));
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumber(serialNumber)
                .filter(containerEntity -> !containerEntity.getRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Container not found with serial number: " + serialNumber));
    }
}
