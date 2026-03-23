package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.common.Validator;
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
    private static final String CONTAINER_TYPE_NOT_FOUND_WITH_NAME = "Container type not found with name: %s";
    private static final String CONTAINER_TYPE_NOT_FOUND_WITH_ID = "Container type not found with id: %s";
    private static final String CONTAINER_TYPE_WITH_NAME_EXIST = "Container type with name already exists: %s";

    private final ContainerTypeRepository containerTypeRepository;
    private final Validator validator;

    public ContainerTypeEntity getContainerTypeByName(final String containerTypeName) {
        return containerTypeRepository.findByName(containerTypeName)
                .orElseThrow(
                        () -> new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_NAME.formatted(containerTypeName)));
    }

    public List<ContainerTypeResponse> getAllContainerTypes() {
        return containerTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContainerTypeResponse getContainerTypeById(final Long containerTypeId) {
        return containerTypeRepository.findById(containerTypeId)
                .map(this::mapToResponse)
                .orElseThrow(
                        () -> new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_ID.formatted(containerTypeId)));
    }

    @Transactional
    public ContainerTypeResponse createContainerType(final ContainerTypeRequest request) {
        validator.validateUniqueness(request.name(), containerTypeRepository::findByName,
                CONTAINER_TYPE_WITH_NAME_EXIST.formatted(request.name())
        );
        ContainerTypeEntity entity = mapToEntity(request);
        ContainerTypeEntity saved = containerTypeRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public ContainerTypeResponse updateContainerType(final Long containerTypeId, final ContainerTypeRequest request) {
        ContainerTypeEntity entity = containerTypeRepository.findById(containerTypeId)
                .orElseThrow(
                        () -> new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_ID.formatted(containerTypeId)));

        if (!validator.isNullOrEmpty(request.name())) {
            validator.validateUniqueness(request.name(), containerTypeRepository::findByName,
                    CONTAINER_TYPE_WITH_NAME_EXIST.formatted(request.name())
            );
            entity.setName(request.name());
        }

        if (!validator.isNullOrEmpty(request.description())) {
            entity.setDescription(request.description());
        }

        if (validator.isPositiveBigDecimal(request.capacity())) {
            entity.setCapacity(request.capacity());
        }

        ContainerTypeEntity updated = containerTypeRepository.save(entity);
        return mapToResponse(updated);
    }

    @Transactional
    public void removeContainerTypeById(final Long containerTypeId) {
        if (!containerTypeRepository.existsById(containerTypeId)) {
            throw new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_ID.formatted(containerTypeId));
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
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCapacity(request.capacity());
        return entity;
    }
}
