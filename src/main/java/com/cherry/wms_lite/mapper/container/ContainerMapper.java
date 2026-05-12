package com.cherry.wms_lite.mapper.container;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.entity.InventoryEntity;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class ContainerMapper {
    private final MessageService messageService;

    public ContainerResponse toResponse(final ContainerEntity container) {
        return new ContainerResponse(
                container.getId(),
                container.getContainerType().getName(),
                container.getSerialNumber(),
                container.getCreatedAt().truncatedTo(ChronoUnit.MILLIS),
                container.getStatus(),
                getLocationName(container)
        );
    }

    private String getLocationName(final ContainerEntity container) {
        try {
            return container.getAttachedToInventoryEntity().getStorageLocation() != null
                    ? container.getAttachedToInventoryEntity().getStorageLocation().getName()
                    : container.getAttachedToInventoryEntity().getContainer().getSerialNumber();
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_DOES_NOT_HAVE_VALID_STORAGE,
                            container.getSerialNumber()));
        }
    }

    public ContainerEntity toEntity(final ContainerRequest request, final ContainerTypeEntity containerType,
                                    final InventoryEntity inventory, final InventoryEntity attachedToInventory)
    {
        ContainerEntity container = new ContainerEntity();
        container.setSerialNumber(request.serialNumber());
        container.setContainerType(containerType);
        container.setInventoryEntity(inventory);
        container.setAttachedToInventoryEntity(attachedToInventory);
        container.setStatus(request.status());
        container.setCreatedAt(Instant.now());
        container.setRemoved(false);
        return container;
    }
}
