package com.cherry.wms_lite.service.inventory;

import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryEntity getNewInventory() {
        InventoryEntity inventoryEntity = new InventoryEntity();
        return inventoryRepository.save(inventoryEntity);
    }
}
