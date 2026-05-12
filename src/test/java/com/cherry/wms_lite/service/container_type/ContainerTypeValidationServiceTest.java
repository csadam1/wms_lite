package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.model.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ContainerTypeValidationServiceTest {
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final Long ID_3 = 3L;
    private static final BigDecimal CAPACITY_1 = BigDecimal.valueOf(4.0);
    private static final BigDecimal CAPACITY_2 = BigDecimal.valueOf(5.0);
    private static final BigDecimal CAPACITY_3 = BigDecimal.valueOf(100.0);
    private static final BigDecimal NEW_CAPACITY = BigDecimal.valueOf(10.0);
    private static final BigDecimal NEW_CAPACITY_0 = BigDecimal.ZERO;
    private static final BigDecimal QUANTITY_1 = BigDecimal.valueOf(1.0);
    private static final BigDecimal QUANTITY_2 = BigDecimal.valueOf(2.0);

    @InjectMocks
    private ContainerTypeValidationService containerTypeValidationService;

    @Test
    void containerContentIsLessOrEqualThanNewCapacity_trueEmpty() {
        // Container is empty, total quantity is 0 which is equals to new capacity 0
        // Arrange
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).build();
        ContainerEntity container = ContainerEntity.builder().id(ID_1).inventory(inventory).build();

        // Act and Assert
        assertTrue(
                containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(container, NEW_CAPACITY_0));
    }

    @Test
    void containerContentIsLessOrEqualThanNewCapacity_trueOnlyItems() {
        // Put 2 items with 1 and 2 quantities into the container, total quantity is 3 which is less than new capacity 10
        // Arrange
        List<ItemEntity> items = get2ElemItemList();
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).items(items).build();
        ContainerEntity container = ContainerEntity.builder().id(ID_1).inventory(inventory).build();

        // Act and Assert
        assertTrue(
                containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(container, NEW_CAPACITY));
    }

    @Test
    void containerContentIsLessOrEqualThanNewCapacity_trueOnlyContainers() {
        // Put 2 containers with 4 and 5 capacities into the container, total quantity is 9 which is less than new capacity 10
        // Arrange
        List<ContainerEntity> containers = get2ElemContainerList();
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).containers(containers).build();
        ContainerEntity container = ContainerEntity.builder().id(ID_1).inventory(inventory).build();

        // Act and Assert
        assertTrue(
                containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(container, NEW_CAPACITY));
    }

    @Test
    void containerContentIsLessOrEqualThanNewCapacity_trueContainerAndItemEqualNewCapacity() {
        // Put 2 containers with 4 and 5 capacities, and 1 item with 1 quantity into the container, total quantity is 10 which equals to new capacity 10
        // Arrange
        List<ContainerEntity> containers = get2ElemContainerList();
        List<ItemEntity> items = List.of(ItemEntity.builder().id(ID_1).quantity(QUANTITY_1).build());
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).items(items).containers(containers).build();
        ContainerEntity container = ContainerEntity.builder().id(ID_1).inventory(inventory).build();

        // Act and Assert
        assertTrue(
                containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(container, NEW_CAPACITY));
    }

    @Test
    void containerContentIsLessOrEqualThanNewCapacity_falseContainerAndItemExceedNewCapacity() {
        // Put 2 containers with 4 and 5 capacities, and 1 item with 2 quantities into the container, total quantity is 11 which exceeds new capacity 10
        // Arrange
        List<ContainerEntity> containers = get2ElemContainerList();
        List<ItemEntity> items = List.of(ItemEntity.builder().id(ID_1).quantity(QUANTITY_2).build());
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).items(items).containers(containers).build();
        ContainerEntity container = ContainerEntity.builder().id(ID_1).inventory(inventory).build();

        // Act and Assert
        assertFalse(
                containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(container, NEW_CAPACITY));
    }

    @Test
    void containerFitsIntoLocation_trueContainerInStorageLocation() {
        // Container is put into a storage location, that can store any container regardless of its capacity
        // Arrange
        StorageLocationEntity storageLocation = StorageLocationEntity.builder().id(ID_1).build();
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).storageLocation(storageLocation).build();
        ContainerTypeEntity containerType = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerEntity container =
                ContainerEntity.builder().id(ID_1).attachedToInventory(inventory).containerType(containerType)
                        .build();

        // Act and Assert
        assertTrue(containerTypeValidationService.containerFitsIntoLocation(container, NEW_CAPACITY));
    }

    @Test
    void containerFitsIntoLocation_trueContainerInContainer() {
        // Container is put into a parent container. The container will take up 10 quantity in the parent container which has a capacity of 100. The parent container has other items in it too
        // Arrange
        List<ItemEntity> items = get2ElemItemList();
        InventoryEntity parentInventory = InventoryEntity.builder().id(ID_1).items(items).build();
        ContainerTypeEntity containerType1 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_3).build();
        ContainerEntity parentContainer =
                ContainerEntity.builder().id(ID_1).inventory(parentInventory).containerType(containerType1).build();

        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).container(parentContainer).build();
        ContainerTypeEntity containerType2 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerEntity container =
                ContainerEntity.builder().id(ID_1).attachedToInventory(inventory).containerType(containerType2)
                        .build();

        // Act and Assert
        assertTrue(containerTypeValidationService.containerFitsIntoLocation(container, NEW_CAPACITY));
    }

    @Test
    void containerFitsIntoLocation_falseContainerInContainerWithContainerAndItemExceedNewCapacity() {
        // Container is put into a parent container. The container will take up 10 quantity in the parent container which has a capacity of 4
        // Arrange
        List<ContainerEntity> containers = get2ElemContainerList();
        InventoryEntity parentInventory = InventoryEntity.builder().id(ID_1).containers(containers).build();
        ContainerTypeEntity containerType1 = ContainerTypeEntity.builder().id(ID_3).capacity(CAPACITY_1).build();
        ContainerEntity parentContainer =
                ContainerEntity.builder().id(ID_1).inventory(parentInventory).containerType(containerType1).build();

        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).container(parentContainer).build();
        ContainerTypeEntity containerType2 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerEntity container =
                ContainerEntity.builder().id(ID_1).attachedToInventory(inventory).containerType(containerType2)
                        .build();

        // Act and Assert
        assertFalse(containerTypeValidationService.containerFitsIntoLocation(container, NEW_CAPACITY));
    }

    @Test
    void isContainerTypeChangeValid_true() {
        // Container has elements with 10 quantity in it. It is put into a parent container with a capacity of 100.
        // The container's new capacity is updated to 10 which fits into the parent container and has enough space
        // for its elements.
        // Arrange
        List<ItemEntity> parentItems = get2ElemItemList();
        InventoryEntity parentInventory = InventoryEntity.builder().id(ID_1).items(parentItems).build();
        ContainerTypeEntity containerType1 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_3).build();
        ContainerEntity parentContainer =
                ContainerEntity.builder().id(ID_1).inventory(parentInventory).containerType(containerType1).build();
        parentInventory.setContainer(parentContainer);

        List<ContainerEntity> containers = get2ElemContainerList();
        List<ItemEntity> items = List.of(ItemEntity.builder().id(ID_1).quantity(QUANTITY_1).build());
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).items(items).containers(containers).build();
        ContainerTypeEntity containerType2 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerEntity container = ContainerEntity.builder()
                .id(ID_1)
                .attachedToInventory(parentInventory)
                .inventory(inventory)
                .containerType(containerType2)
                .build();

        // Act and Assert
        assertTrue(containerTypeValidationService.isContainerTypeChangeValid(container, NEW_CAPACITY));
    }

    @Test
    void isContainerTypeChangeValid_false() {
        // Container has elements with 10 quantity in it. It is put into a parent container with a capacity of 100.
        // The container's new capacity is updated to 0. Even though the container will fit into the parent container
        // it does not have enough space for its elements, so the change is not valid.
        // Arrange
        List<ItemEntity> parentItems = get2ElemItemList();
        InventoryEntity parentInventory = InventoryEntity.builder().id(ID_1).items(parentItems).build();
        ContainerTypeEntity containerType1 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_3).build();
        ContainerEntity parentContainer =
                ContainerEntity.builder().id(ID_1).inventory(parentInventory).containerType(containerType1).build();
        parentInventory.setContainer(parentContainer);

        List<ContainerEntity> containers = get2ElemContainerList();
        List<ItemEntity> items = List.of(ItemEntity.builder().id(ID_1).quantity(QUANTITY_1).build());
        InventoryEntity inventory = InventoryEntity.builder().id(ID_1).items(items).containers(containers).build();
        ContainerTypeEntity containerType2 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerEntity container = ContainerEntity.builder()
                .id(ID_1)
                .attachedToInventory(parentInventory)
                .inventory(inventory)
                .containerType(containerType2)
                .build();

        // Act and Assert
        assertFalse(containerTypeValidationService.isContainerTypeChangeValid(container, NEW_CAPACITY_0));
    }

    private List<ContainerEntity> get2ElemContainerList() {
        ContainerTypeEntity containerType1 = ContainerTypeEntity.builder().id(ID_1).capacity(CAPACITY_1).build();
        ContainerTypeEntity containerType2 = ContainerTypeEntity.builder().id(ID_2).capacity(CAPACITY_2).build();
        return List.of(
                ContainerEntity.builder().id(ID_2).containerType(containerType1).build(),
                ContainerEntity.builder().id(ID_3).containerType(containerType2).build());
    }

    private List<ItemEntity> get2ElemItemList() {
        return List.of(
                ItemEntity.builder().id(ID_1).quantity(QUANTITY_1).build(),
                ItemEntity.builder().id(ID_2).quantity(QUANTITY_2).build());
    }
}
