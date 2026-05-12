package com.cherry.wms_lite.mapper.container_type;

import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class ContainerTypeMapper {
    public ContainerTypeResponse toResponse(final ContainerTypeEntity containerType) {
        return new ContainerTypeResponse(
                containerType.getId(),
                containerType.getName(),
                containerType.getDescription(),
                containerType.getCapacity());
    }

    public ContainerTypeEntity toEntity(final ContainerTypeRequest request) {
        ContainerTypeEntity entity = new ContainerTypeEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCapacity(request.capacity());
        return entity;
    }
}
