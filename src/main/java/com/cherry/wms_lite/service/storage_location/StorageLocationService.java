package com.cherry.wms_lite.service.storage_location;

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
    private static final String SL_NOT_FOUND_WITH_NAME = "Storage Location not found with name: %s";
    private static final String SL_NOT_FOUND_WITH_ID = "Storage Location not found with id: %s";
    private static final String SL_WITH_NAME_EXIST = "Storage Location with name already exists: %s";

    private final StorageLocationRepository storageLocationRepository;
    private final InventoryService inventoryService;
    private final Validator validator;

    public InventoryEntity getStorageLocationInventoryByName(final String name) {
        return storageLocationRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(SL_NOT_FOUND_WITH_NAME.formatted(name)))
                .getInventoryEntity();
    }

    public StorageLocationResponse getStorageLocationById(final Long storageLocationId) {
        return storageLocationRepository.findById(storageLocationId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException(SL_NOT_FOUND_WITH_ID.formatted(storageLocationId)));
    }

    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StorageLocationResponse createStorageLocation(final StorageLocationRequest request) {
        validator.validateUniqueness(request.name(), storageLocationRepository::findByName,
                SL_WITH_NAME_EXIST.formatted(request.name())
        );
        StorageLocationEntity entity = mapToEntity(request);
        entity.setInventoryEntity(inventoryService.getNewInventory());
        StorageLocationEntity saved = storageLocationRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public StorageLocationResponse updateStorageLocation(final Long storageLocationId,
                                                         final StorageLocationRequest request)
    {
        StorageLocationEntity storageLocationEntity = storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new EntityNotFoundException(SL_NOT_FOUND_WITH_ID.formatted(storageLocationId)));

        if (!validator.isNullOrEmpty(request.name())) {
            validator.validateUniqueness(request.name(), storageLocationRepository::findByName,
                    SL_WITH_NAME_EXIST.formatted(request.name())
            );
            storageLocationEntity.setName(request.name());
        }

        if (!validator.isNullOrEmpty(request.description())) {
            storageLocationEntity.setDescription(request.description());
        }

        StorageLocationEntity updated = storageLocationRepository.save(storageLocationEntity);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteStorageLocationById(final Long storageLocationId) {
        if (!storageLocationRepository.existsById(storageLocationId)) {
            throw new EntityNotFoundException(SL_NOT_FOUND_WITH_ID.formatted(storageLocationId));
        }
        storageLocationRepository.deleteById(storageLocationId);
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
