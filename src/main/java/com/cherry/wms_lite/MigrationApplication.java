package com.cherry.wms_lite;

import com.cherry.wms_lite.model.entity.*;
import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.repository.InventoryRepository;
import com.cherry.wms_lite.repository.ItemRepository;
import com.cherry.wms_lite.repository.StorageLocationRepository;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.Instant;

@SpringBootApplication
public class MigrationApplication {

    @Bean
    CommandLineRunner initData(ContainerTypeRepository containerTypeRepository,
                               ContainerRepository containerRepository,
                               InventoryRepository inventoryRepository,
                               StorageLocationRepository storageLocationRepository,
                               ItemRepository itemRepository) {
        return args -> {
            InventoryEntity sl1InventoryEntity = getInventoryEntity();
            inventoryRepository.save(sl1InventoryEntity);
            StorageLocationEntity sl1 = getMainSlEntity(sl1InventoryEntity);
            storageLocationRepository.save(sl1);

            ContainerTypeEntity box = getBoxEntity();
            box = containerTypeRepository.save(box);
            InventoryEntity box001Inventory = getInventoryEntity();
            inventoryRepository.save(box001Inventory);
            ContainerEntity box001 = getContainerEntity("BOX-001", box, box001Inventory, sl1InventoryEntity);
            containerRepository.save(box001);

            ContainerTypeEntity crate = getCrateEntity();
            crate = containerTypeRepository.save(crate);
            InventoryEntity crt999Inventory = getInventoryEntity();
            inventoryRepository.save(crt999Inventory);
            ContainerEntity crt999 = getContainerEntity("CRT-999", crate, crt999Inventory, sl1InventoryEntity);
            containerRepository.save(crt999);

            InventoryEntity sl2InventoryEntity = getInventoryEntity();
            inventoryRepository.save(sl2InventoryEntity);
            StorageLocationEntity sl2 = get0010SlEntity(sl2InventoryEntity);
            storageLocationRepository.save(sl2);

            ItemEntity nail100qty = get100NailEntity(crt999Inventory);
            itemRepository.save(nail100qty);
            ItemEntity steeringWheel = getSteeringWheelItem(sl2InventoryEntity);
            itemRepository.save(steeringWheel);
        };
    }

    private ItemEntity get100NailEntity(final InventoryEntity inventoryEntity) {
        return ItemEntity.builder()
                .serialNumber("TK-NAIL-PACK-12345")
                .material("TK-NAIL-0002")
                .attachedToInventory(inventoryEntity)
                .quantity(BigDecimal.valueOf(100))
                .build();
    }

    private ItemEntity getSteeringWheelItem(final InventoryEntity inventoryEntity) {
        return ItemEntity.builder()
                .serialNumber("TK-STW-12345")
                .material("TK-ST-WHEEL-0001")
                .attachedToInventory(inventoryEntity)
                .quantity(BigDecimal.valueOf(1))
                .build();
    }

    private InventoryEntity getInventoryEntity() {
        return InventoryEntity.builder().build();
    }

    private ContainerEntity getContainerEntity(final String name,
                                               final ContainerTypeEntity crate,
                                               final InventoryEntity inventoryEntity,
                                               final InventoryEntity attachedToInventoryEntity) {
        return ContainerEntity
                .builder()
                .serialNumber(name)
                .containerType(crate)
                .inventory(inventoryEntity)
                .attachedToInventory(attachedToInventoryEntity)
                .createdAt(Instant.now())
                .status(ContainerStatusEnum.CLOSED)
                .removed(false)
                .build();
    }

    private StorageLocationEntity getMainSlEntity(final InventoryEntity inventoryEntity) {
        return StorageLocationEntity
                .builder()
                .name("Main Warehouse")
                .description("Primary storage location for all inventory")
                .inventory(inventoryEntity)
                .build();
    }

    private StorageLocationEntity get0010SlEntity(final InventoryEntity inventoryEntity) {
        return StorageLocationEntity
                .builder()
                .name("Factory Floor Storage 0010")
                .description("Factory Floor Storage 0010")
                .inventory(inventoryEntity)
                .build();
    }

    private ContainerTypeEntity getCrateEntity() {
        return ContainerTypeEntity
                .builder()
                .name("Crate")
                .description("Wooden crate")
                .capacity(BigDecimal.valueOf(100))
                .build();
    }

    private ContainerTypeEntity getBoxEntity() {
        return ContainerTypeEntity
                .builder()
                .name("Box")
                .description("Standard cardboard box")
                .capacity(BigDecimal.valueOf(12))
                .build();
    }
}
