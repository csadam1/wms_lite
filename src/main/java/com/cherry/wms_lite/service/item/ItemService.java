package com.cherry.wms_lite.service.item;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.ItemEntity;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.item.ItemRequest;
import com.cherry.wms_lite.model.response.item.ItemResponse;
import com.cherry.wms_lite.repository.ItemRepository;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.service.container_type.ContainerTypeValidationService;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final Validator validator;
    private final MessageService messageService;
    private final StorageLocationService storageLocationService;
    private final ContainerTypeValidationService containerTypeValidationService;
    private final ContainerRepository containerRepository;

    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ItemResponse getItemById(final Long itemId) {
        return itemRepository.findById(itemId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.ITEM_NOT_FOUND_WITH_ID, itemId)));
    }

    @Transactional
    public ItemResponse createItem(final ItemRequest request) {
        validateCreateRequest(request);

        InventoryEntity inventoryEntity = getInventoryEntityForItem(request.locationTypeEnum(), request.locationName());
        ItemEntity entity = mapToEntity(request, inventoryEntity);
        ItemEntity saved = itemRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public ItemResponse updateItem(final Long itemId, final ItemRequest request) {
        // TODO: TBC
        return null;
    }

    @Transactional
    public void deleteItemById(final Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.ITEM_NOT_FOUND_WITH_ID, itemId));
        }
        itemRepository.deleteById(itemId);
    }

    private InventoryEntity getInventoryEntityForItem(final LocationTypeEnum locationTypeEnum,
                                                      final String locationName)
    {
        try {
            return locationTypeEnum.equals(LocationTypeEnum.STORAGE_LOCATION)
                    ? storageLocationService.getStorageLocationInventoryByName(locationName)
                    : getContainerEntityByName(locationName).getInventoryEntity();
        } catch (NullPointerException e) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.LOCATION_NAME_DOES_NOT_EXIST, locationName));
        }
    }

    private ItemEntity mapToEntity(final ItemRequest request, final InventoryEntity attachedToInventoryEntity) {
        return ItemEntity.builder()
                .serialNumber(request.serialNumber())
                .material(request.material())
                .attachedToInventoryEntity(attachedToInventoryEntity)
                .quantity(request.quantity())
                .build();
    }

    private void validateCreateRequest(final ItemRequest request) {
        validator.validateUniqueness(request.serialNumber(), itemRepository::findBySerialNumber,
                messageService.getMessage(ExceptionMessageKeys.ITEM_SERIAL_NUMBER_EXISTS, request.serialNumber()));

        if (request.locationTypeEnum().equals(LocationTypeEnum.STORAGE_LOCATION)) {
            storageLocationService.getStorageLocationByName(request.locationName());
        } else {
            // Does container have enough capacity for new item
            ContainerEntity parentContainer = getContainerEntityByName(request.locationName());
            boolean isContainerOverloaded =
                    !containerTypeValidationService.containerContentIsLessOrEqualThanNewCapacity(parentContainer,
                            request.quantity());

            if (isContainerOverloaded) {
                throw new IllegalStateException(
                        messageService.getMessage(ExceptionMessageKeys.CONTAINER_CAPACITY_EXCEEDED,
                                parentContainer.getSerialNumber()));
            }
        }
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumberAndRemovedFalse(serialNumber)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.CONTAINER_NOT_FOUND_WITH_SERIAL, serialNumber)));
    }

    private ItemResponse mapToResponse(final ItemEntity itemEntity) {
        return new ItemResponse(
                itemEntity.getId(),
                itemEntity.getSerialNumber(),
                itemEntity.getMaterial(),
                getStorageName(itemEntity),
                itemEntity.getQuantity());
    }

    private String getStorageName(final ItemEntity itemEntity) {
        try {
            InventoryEntity inventoryEntity = itemEntity.getAttachedToInventoryEntity();
            return inventoryEntity.getStorageLocation() != null
                    ? inventoryEntity.getStorageLocation().getName()
                    : inventoryEntity.getContainer().getSerialNumber();
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.ITEM_DOES_NOT_HAVE_VALID_STORAGE,
                            itemEntity.getSerialNumber()));
        }
    }
}
