package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerTypeService {
    private final ContainerTypeRepository containerTypeRepository;

    public ContainerTypeEntity getContainerTypeByName(final String containerTypeName) {
        return containerTypeRepository.findByName(containerTypeName)
                .orElseThrow(() -> new EntityNotFoundException("Container type not found: " + containerTypeName));
    }
}
