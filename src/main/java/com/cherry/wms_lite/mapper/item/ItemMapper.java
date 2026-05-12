package com.cherry.wms_lite.mapper.item;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.ItemEntity;
import com.cherry.wms_lite.model.request.item.ItemRequest;
import com.cherry.wms_lite.model.response.item.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final MessageService messageService;

    public ItemResponse mapToResponse(final ItemEntity item) {
        return new ItemResponse(
                item.getId(),
                item.getSerialNumber(),
                item.getMaterial(),
                getStorageName(item),
                item.getQuantity());
    }

    private String getStorageName(final ItemEntity item) {
        try {
            InventoryEntity inventory = item.getAttachedToInventory();
            return inventory.getStorageLocation() != null
                    ? inventory.getStorageLocation().getName()
                    : inventory.getContainer().getSerialNumber();
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.ITEM_DOES_NOT_HAVE_VALID_STORAGE,
                            item.getSerialNumber()));
        }
    }

    public ItemEntity toEntity(final ItemRequest request, final InventoryEntity attachedToInventory) {
        ItemEntity entity = new ItemEntity();
        entity.setSerialNumber(request.serialNumber());
        entity.setMaterial(request.material());
        entity.setAttachedToInventory(attachedToInventory);
        entity.setQuantity(request.quantity());
        return entity;
    }
}
