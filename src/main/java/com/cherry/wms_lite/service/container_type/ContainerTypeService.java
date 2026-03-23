package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerTypeService {
    private final ContainerTypeRepository containerTypeRepository;
    public ContainerTypeEntity getContainerTypeByName(final String containerTypeName) {
        return containerTypeRepository.findByName(containerTypeName)
                .orElseThrow(() -> new EntityNotFoundException("Container type not found with name: " + containerTypeName));
    }

    public List<ContainerTypeResponse> getAllContainerTypes() {
        return containerTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContainerTypeResponse getContainerTypeById(final Long containerTypeId) {
        return containerTypeRepository.findById(containerTypeId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Container type not found with id: " + containerTypeId));
    }

    @Transactional
    public ContainerTypeResponse createContainerType(final ContainerTypeRequest request) {
        ContainerTypeEntity entity = mapToEntity(request);
        ContainerTypeEntity saved = containerTypeRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public ContainerTypeResponse updateContainerType(final Long containerTypeId, final ContainerTypeRequest request) {
        ContainerTypeEntity entity = containerTypeRepository.findById(containerTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Container type not found with id: " + containerTypeId));

        updateEntityFromRequest(entity, request);
        ContainerTypeEntity updated = containerTypeRepository.save(entity);
        return mapToResponse(updated);
    }

    @Transactional
    public void removeContainerTypeById(final Long containerTypeId) {
        if (!containerTypeRepository.existsById(containerTypeId)) {
            throw new EntityNotFoundException("Container type not found with id: " + containerTypeId);
        }
        containerTypeRepository.deleteById(containerTypeId);
    }

    private ContainerTypeResponse mapToResponse(final ContainerTypeEntity entity) {
        return new ContainerTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCapacity()
        );
    }

    private ContainerTypeEntity mapToEntity(final ContainerTypeRequest request) {
        ContainerTypeEntity entity = new ContainerTypeEntity();
        updateEntityFromRequest(entity, request);
        return entity;
    }

    private void updateEntityFromRequest(final ContainerTypeEntity entity, final ContainerTypeRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCapacity(request.capacity());
    }
}
