package com.cherry.wms_lite.service.storage_location;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.StorageLocationEntity;
import com.cherry.wms_lite.model.request.storage_location.StorageLocationRequest;
import com.cherry.wms_lite.model.response.storage_location.StorageLocationResponse;
import com.cherry.wms_lite.repository.StorageLocationRepository;
import com.cherry.wms_lite.service.inventory.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageLocationService {
    private final StorageLocationRepository storageLocationRepository;
    private final InventoryService inventoryService;
    private final Validator validator;
    private final MessageService messageService;

    public StorageLocationEntity getStorageLocationByName(final String name) {
        return storageLocationRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NOT_FOUND_WITH_NAME, name)));
    }

    public InventoryEntity getStorageLocationInventoryByName(final String name) {
        return getStorageLocationByName(name).getInventoryEntity();
    }

    public StorageLocationResponse getStorageLocationById(final Long storageLocationId) {
        return storageLocationRepository.findById(storageLocationId)
                .map(this::mapToResponse)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NOT_FOUND_WITH_ID,
                                        storageLocationId)));
    }

    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StorageLocationResponse createStorageLocation(final StorageLocationRequest request) {
        validator.validateUniqueness(request.name(), storageLocationRepository::findByName,
                messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NAME_EXISTS, request.name())
        );
        StorageLocationEntity entity = mapToEntity(request);
        entity.setInventoryEntity(inventoryService.createNewInventory());
        StorageLocationEntity saved = storageLocationRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public StorageLocationResponse updateStorageLocation(final Long storageLocationId,
                                                         final StorageLocationRequest request)
    {
        StorageLocationEntity storageLocationEntity = storageLocationRepository.findById(storageLocationId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NOT_FOUND_WITH_ID,
                                        storageLocationId)));

        updateNameIfProvided(request, storageLocationEntity);
        updateDescriptionIfProvided(request, storageLocationEntity);

        StorageLocationEntity updated = storageLocationRepository.save(storageLocationEntity);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteStorageLocationById(final Long storageLocationId) {
        if (!storageLocationRepository.existsById(storageLocationId)) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NOT_FOUND_WITH_ID, storageLocationId));
        }
        storageLocationRepository.deleteById(storageLocationId);
    }

    private void updateDescriptionIfProvided(final StorageLocationRequest request,
                                             final StorageLocationEntity storageLocationEntity)
    {
        if (!validator.isNullOrEmpty(request.description())) {
            storageLocationEntity.setDescription(request.description());
        }
    }

    private void updateNameIfProvided(final StorageLocationRequest request,
                                      final StorageLocationEntity storageLocationEntity)
    {
        if (!validator.isNullOrEmpty(request.name())) {
            validator.validateUniqueness(request.name(), storageLocationRepository::findByName,
                    messageService.getMessage(ExceptionMessageKeys.STORAGE_LOCATION_NAME_EXISTS, request.name())
            );
            storageLocationEntity.setName(request.name());
        }
    }

    private StorageLocationEntity mapToEntity(final StorageLocationRequest request) {
        StorageLocationEntity entity = new StorageLocationEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        return entity;
    }

    private StorageLocationResponse mapToResponse(final StorageLocationEntity storageLocationEntity) {
        return new StorageLocationResponse(
                storageLocationEntity.getId(),
                storageLocationEntity.getName(),
                storageLocationEntity.getDescription());
    }
}
