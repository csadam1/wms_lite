package com.cherry.wms_lite.service.storage_location;

import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.repository.StorageLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageLocationService {
    private final StorageLocationRepository storageLocationRepository;

    public InventoryEntity getStorageLocationInventoryByName(final String name) {
        return storageLocationRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Storage Location not found: " + name))
                .getInventoryEntity();
    }
}
