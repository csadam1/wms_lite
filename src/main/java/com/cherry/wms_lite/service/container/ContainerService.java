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
        return containerRepository
                .findAll()
                .stream()
                .filter(containerEntity -> !containerEntity.getRemoved())
                .map(this::mapToGetContainerResponse)
                .toList();
    }

    public ContainerResponse getContainerById(final Long containerId) {
        return this.mapToGetContainerResponse(this.getContainerEntityById(containerId));
    }

    @Transactional
    public ContainerResponse createContainer(final ContainerRequest request) {
        this.validateSerialNumberUniqueness(request.serialNumber());

        ContainerTypeEntity containerType = containerTypeService.getContainerTypeByName(request.containerTypeName());
        InventoryEntity attachedToInventoryEntity = request.locationTypeEnum().equals(LocationTypeEnum.CONTAINER)
                ? this.getContainerInventory(request.locationName())
                : storageLocationService.getStorageLocationInventoryByName(request.locationName());

        ContainerEntity containerEntity = ContainerEntity.builder()
                .serialNumber(request.serialNumber())
                .containerType(containerType)
                .inventoryEntity(inventoryService.getNewInventory())
                .attachedToInventoryEntity(attachedToInventoryEntity)
                .createdAt(Instant.now())
                .status(request.status())
                .removed(false)
                .build();

        ContainerEntity savedContainer = containerRepository.save(containerEntity);
        return this.mapToGetContainerResponse(savedContainer);
    }

    @Transactional
    public ContainerResponse updateContainer(final Long containerId, final ContainerRequest request) {
        ContainerEntity containerEntity = this.getContainerEntityById(containerId);
        // Update serial number if provided
        if (request.serialNumber() != null && !request.serialNumber().isBlank()) {
            this.validateSerialNumberUniqueness(request.serialNumber());
            containerEntity.setSerialNumber(request.serialNumber());
        }

        // Update status if provided
        if (request.status() != null) {
            containerEntity.setStatus(request.status());
        }

        // Update container type if provided
        if (request.containerTypeName() != null && !request.containerTypeName().isBlank()) {
            ContainerTypeEntity containerType =
                    containerTypeService.getContainerTypeByName(request.containerTypeName());
            containerEntity.setContainerType(containerType);
        }

        // Update location if provided
        if (request.locationName() != null && !request.locationName().isBlank()
                && request.locationTypeEnum() != null)
        {
            InventoryEntity attachedToInventoryEntity = this.getContainerAttachedToInventory(request.locationName(),
                    request.locationTypeEnum());
            containerEntity.setAttachedToInventoryEntity(attachedToInventoryEntity);
        }

        ContainerEntity updatedContainer = containerRepository.save(containerEntity);
        return this.mapToGetContainerResponse(updatedContainer);
    }

    @Transactional
    public void removeContainer(final Long containerId) {
        if (!containerRepository.existsById(containerId)) {
            throw new EntityNotFoundException("Container not found with id: " + containerId);
        }

        ContainerEntity containerEntity = this.getContainerEntityById(containerId);
        containerEntity.setRemoved(true);
        containerRepository.save(containerEntity);
    }

    private void validateSerialNumberUniqueness(final String serialNumber) {
        containerRepository.findBySerialNumber(serialNumber)
                .ifPresent(container -> {
                    throw new IllegalArgumentException("Container with serial number already exists: " + serialNumber);
                });
    }

    private InventoryEntity getContainerInventory(final String serialNumber) {
        return this.getContainerEntityByName(serialNumber)
                .getInventoryEntity();
    }

    private InventoryEntity getContainerAttachedToInventory(final String locationName,
                                                            final LocationTypeEnum locationTypeEnum)
    {
        return locationTypeEnum.equals(LocationTypeEnum.CONTAINER) ? this.getContainerInventory(locationName)
                : storageLocationService.getStorageLocationInventoryByName(locationName);
    }

    private ContainerResponse mapToGetContainerResponse(final ContainerEntity containerEntity) {
        return new ContainerResponse(
                containerEntity.getId(),
                containerEntity.getContainerType().getName(),
                containerEntity.getSerialNumber(),
                containerEntity.getCreatedAt().truncatedTo(ChronoUnit.MILLIS),
                containerEntity.getStatus()
        );
    }

    private ContainerEntity getContainerEntityById(final Long id) {
        return containerRepository.findById(id)
                .filter(containerEntity -> !containerEntity.getRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Container not found: " + id));
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumber(serialNumber)
                .filter(containerEntity -> !containerEntity.getRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Container not found: " + serialNumber));
    }
}
