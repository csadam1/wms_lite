package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.ItemEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ContainerTypeValidationService {
    public Boolean isContainerTypeChangeValid(final ContainerEntity container, final BigDecimal newCapacity) {
        return containerContentIsLessOrEqualThanNewCapacity(container, newCapacity)
                && containerFitsIntoLocation(container, newCapacity);
    }

    public Boolean containerContentIsLessOrEqualThanNewCapacity(final ContainerEntity container,
                                                                final BigDecimal newCapacity)
    {
        return getCurrentQuantityForContainer(container, null, null)
                .compareTo(newCapacity) <= 0;
    }

    public Boolean containerFitsIntoLocation(final ContainerEntity container, final BigDecimal newCapacity) {
        if (container.getAttachedToInventory().getStorageLocation() != null) {
            return true;
        }

        ContainerEntity parentContainer = container.getAttachedToInventory().getContainer();
        BigDecimal parentContainerCapacity = parentContainer.getContainerType().getCapacity();
        return getCurrentQuantityForContainer(parentContainer, container.getContainerType(), newCapacity)
                .compareTo(parentContainerCapacity) <= 0;
    }

    private BigDecimal getCurrentQuantityForContainer(final ContainerEntity container,
                                                      final ContainerTypeEntity childContainerType,
                                                      final BigDecimal newCapacity)
    {
        BigDecimal currentItemQuantity = getCurrentItemQuantity(container.getInventory());
        BigDecimal currentContainerQuantity = (childContainerType != null && newCapacity != null)
                ? getCurrentContainerQuantity(container.getInventory(), childContainerType, newCapacity)
                : getCurrentContainerQuantity(container.getInventory());

        return currentContainerQuantity.add(currentItemQuantity);
    }

    private BigDecimal getCurrentContainerQuantity(final InventoryEntity inventoryEntity) {
        return inventoryEntity
                .getContainers()
                .stream()
                .map(ContainerEntity::getContainerType)
                .map(ContainerTypeEntity::getCapacity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getCurrentContainerQuantity(final InventoryEntity inventoryEntity,
                                                   final ContainerTypeEntity childContainerType,
                                                   final BigDecimal newCapacity)
    {
        return inventoryEntity
                .getContainers()
                .stream()
                .map(ContainerEntity::getContainerType)
                .map(innerContainerTypeEntity ->
                        innerContainerTypeEntity.equals(childContainerType)
                                ? newCapacity : innerContainerTypeEntity.getCapacity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getCurrentItemQuantity(final InventoryEntity containerInventory) {
        return containerInventory
                .getItems()
                .stream()
                .map(ItemEntity::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
