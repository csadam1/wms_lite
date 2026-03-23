package com.cherry.wms_lite.service.storage_location;

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

    public InventoryEntity getStorageLocationInventoryByName(final String name) {
        return storageLocationRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Storage Location not found with name: " + name))
                .getInventoryEntity();
    }

    public StorageLocationResponse getStorageLocationById(final Long storageLocationId) {
        return storageLocationRepository.findById(storageLocationId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Storage Location not found with id: " + storageLocationId));
    }

    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StorageLocationResponse createStorageLocation(final StorageLocationRequest request) {
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
                .orElseThrow(() -> new EntityNotFoundException("Storage Location not found with id: " + storageLocationId));

        updateEntityFromRequest(storageLocationEntity, request);
        StorageLocationEntity updated = storageLocationRepository.save(storageLocationEntity);
        return mapToResponse(updated);
    }

    private void updateEntityFromRequest(final StorageLocationEntity entity, final StorageLocationRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
    }

    @Transactional
    public void deleteStorageLocationById(final Long storageLocationId) {
        if (!storageLocationRepository.existsById(storageLocationId)) {
            throw new EntityNotFoundException("Storage Location not found with id: " + storageLocationId);
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
