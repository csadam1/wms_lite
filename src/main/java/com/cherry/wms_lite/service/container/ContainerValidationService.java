package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.ItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ContainerValidationService {
    private final Validator validator;
    private final MessageService messageService;

    public void validateIsContainerFitIntoInventory(final ContainerEntity container) {
        InventoryEntity containerInventory = container.getAttachedToInventoryEntity();
        if (validator.isNullOrEmpty(containerInventory)) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_IS_NOT_ANY_IN_INVENTORY));
        }

        // Container is placed in a Storage Location with unlimited storage capacity
        if (!validator.isNullOrEmpty(containerInventory.getStorageLocation())) {
            return;
        }

        // Container is placed in a Parent Container with limited storage capacity
        if (!validator.isNullOrEmpty(containerInventory.getContainer())) {
            BigDecimal containerSize = container.getContainerType().getCapacity();

            ContainerEntity parentContainer = containerInventory.getContainer();
            BigDecimal parentContainerCapacity = parentContainer.getContainerType().getCapacity();
            BigDecimal parentContainerQuantity = getContainerCurrentQuantity(parentContainer);

            // Is Parent Container Capacity exceeded by its quantity plus container size
            if (parentContainerCapacity.compareTo(containerSize.add(parentContainerQuantity)) < 0) {
                throw new IllegalStateException(messageService.getMessage(
                        ExceptionMessageKeys.CONTAINER_CAPACITY_EXCEEDED, parentContainer.getSerialNumber()));
            }

            return;
        }

        throw new IllegalStateException(messageService.getMessage(ExceptionMessageKeys.PARENT_INVENTORY_IS_UNATTACHED));
    }

    public void validateIsContentFitIntoContainerInventory(final ContainerEntity container) {
        BigDecimal containerCapacity = container.getContainerType().getCapacity();
        BigDecimal containerQuantity = getContainerCurrentQuantity(container);

        // Is Container Capacity exceeded by its Quantity
        if (containerCapacity.compareTo(containerQuantity) < 0) {
            throw new IllegalStateException(messageService.getMessage(
                    ExceptionMessageKeys.CONTAINER_CAPACITY_EXCEEDED, container.getSerialNumber()));
        }
    }

    private BigDecimal getContainerCurrentQuantity(final ContainerEntity container) {
        InventoryEntity containerInventory = container.getInventoryEntity();
        return getCurrentItemQuantity(containerInventory)
                .add(getCurrentContainerQuantity(containerInventory));
    }

    private BigDecimal getCurrentItemQuantity(final InventoryEntity containerInventory) {
        return containerInventory
                .getItems()
                .stream()
                .map(ItemEntity::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getCurrentContainerQuantity(final InventoryEntity inventoryEntity) {
        return inventoryEntity
                .getContainers()
                .stream()
                .map(ContainerEntity::getContainerType)
                .map(ContainerTypeEntity::getCapacity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
