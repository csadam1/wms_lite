package com.cherry.wms_lite.mapper.storage_location;

import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.StorageLocationEntity;
import com.cherry.wms_lite.model.request.storage_location.StorageLocationRequest;
import com.cherry.wms_lite.model.response.storage_location.StorageLocationResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageLocationMapper {
    public StorageLocationResponse toResponse(final StorageLocationEntity storageLocation) {
        return new StorageLocationResponse(
                storageLocation.getId(),
                storageLocation.getName(),
                storageLocation.getDescription());
    }

    public StorageLocationEntity toEntity(final StorageLocationRequest request, final InventoryEntity inventory) {
        StorageLocationEntity entity = new StorageLocationEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setInventoryEntity(inventory);
        return entity;
    }
}
