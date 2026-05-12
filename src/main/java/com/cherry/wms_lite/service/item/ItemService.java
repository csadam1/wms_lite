package com.cherry.wms_lite.service.item;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.mapper.item.ItemMapper;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.entity.ItemEntity;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.item.ItemRequest;
import com.cherry.wms_lite.model.response.item.ItemResponse;
import com.cherry.wms_lite.repository.ItemRepository;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.service.container.ContainerValidationService;
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
    private final ContainerRepository containerRepository;
    private final ItemMapper itemMapper;
    private final ContainerValidationService containerValidationService;

    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    public ItemResponse getItemById(final Long itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.ITEM_NOT_FOUND_WITH_ID, itemId)));
    }

    @Transactional
    public ItemResponse createItem(final ItemRequest request) {
        validateSerialNumberUniqueness(request.serialNumber());

        InventoryEntity attachedToInventory = getInventoryEntityForItem(request.locationTypeEnum(),
                request.locationName());
        ItemEntity item = itemMapper.toEntity(request, attachedToInventory);
        attachedToInventory.getItems().add(item);

        validateContainerCapacity(item, request.locationTypeEnum());

        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse updateItem(final Long itemId, final ItemRequest request) {
        ItemEntity item = getItemEntityById(itemId);

        changeSerialNumberIfProvided(item, request);
        changeMaterialIfProvided(item, request);
        changeQuantityIfProvided(item, request);
        changeLocationIfProvided(item, request);

        validateContainerCapacity(item, getLocationTypeEnum(item));

        return itemMapper.toResponse(item);
    }

    @Transactional
    public void deleteItemById(final Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.ITEM_NOT_FOUND_WITH_ID, itemId));
        }
        itemRepository.deleteById(itemId);
    }

    private LocationTypeEnum getLocationTypeEnum(final ItemEntity item) {
        return validator.isNullOrEmpty(item.getAttachedToInventory().getStorageLocation())
                ? LocationTypeEnum.CONTAINER : LocationTypeEnum.STORAGE_LOCATION;
    }

    private void changeQuantityIfProvided(final ItemEntity item, final ItemRequest request) {
        if (!validator.isNullOrEmpty(request.quantity())) {
            item.setQuantity(request.quantity());
        }
    }

    private void changeLocationIfProvided(final ItemEntity item, final ItemRequest request) {
        if (!validator.isNullOrEmpty(request.locationName()) && !validator.isNullOrEmpty(request.locationTypeEnum())) {
            item.setAttachedToInventory(
                    getContainerAttachedToInventory(request.locationName(), request.locationTypeEnum()));
        }
    }

    private InventoryEntity getContainerAttachedToInventory(final String locationName,
                                                            final LocationTypeEnum locationTypeEnum)
    {
        return LocationTypeEnum.CONTAINER.equals(locationTypeEnum)
                ? getContainerInventory(locationName)
                : storageLocationService.getStorageLocationInventoryByName(locationName);
    }

    private InventoryEntity getContainerInventory(final String serialNumber) {
        return getContainerEntityByName(serialNumber)
                .getInventory();
    }

    private void changeMaterialIfProvided(final ItemEntity item, final ItemRequest request) {
        if (!validator.isNullOrEmpty(request.material())) {
            item.setMaterial(request.material());
        }
    }

    private void changeSerialNumberIfProvided(final ItemEntity item, final ItemRequest request) {
        if (!validator.isNullOrEmpty(request.serialNumber())) {
            validateSerialNumberUniqueness(request.serialNumber());
            item.setSerialNumber(request.serialNumber());
        }
    }

    private ItemEntity getItemEntityById(final Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.ITEM_NOT_FOUND_WITH_ID, itemId)));
    }

    private void validateContainerCapacity(final ItemEntity item, final LocationTypeEnum locationTypeEnum) {
        if (LocationTypeEnum.CONTAINER.equals(locationTypeEnum)) {
            ContainerEntity parentContainer = item.getAttachedToInventory().getContainer();
            containerValidationService.validateIsContentFitIntoContainerInventory(parentContainer);
        }
    }

    private void validateSerialNumberUniqueness(final String serialNumber) {
        validator.validateUniqueness(serialNumber, itemRepository::findBySerialNumber,
                messageService.getMessage(ExceptionMessageKeys.ITEM_SERIAL_NUMBER_EXISTS, serialNumber));
    }

    private InventoryEntity getInventoryEntityForItem(final LocationTypeEnum locationTypeEnum,
                                                      final String locationName) {
        try {
            return LocationTypeEnum.STORAGE_LOCATION.equals(locationTypeEnum)
                    ? storageLocationService.getStorageLocationInventoryByName(locationName)
                    : getContainerEntityByName(locationName).getInventory();
        } catch (NullPointerException e) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.LOCATION_NAME_DOES_NOT_EXIST, locationName));
        }
    }

    private ContainerEntity getContainerEntityByName(final String serialNumber) {
        return containerRepository.findBySerialNumberAndRemovedFalse(serialNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.getMessage(ExceptionMessageKeys.CONTAINER_NOT_FOUND_WITH_SERIAL, serialNumber)));
    }
}
