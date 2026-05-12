package com.cherry.wms_lite.service.container_type;

import com.cherry.wms_lite.common.ExceptionMessageKeys;
import com.cherry.wms_lite.common.MessageService;
import com.cherry.wms_lite.common.Validator;
import com.cherry.wms_lite.mapper.container_type.ContainerTypeMapper;
import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import com.cherry.wms_lite.service.container.ContainerValidationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerTypeService {
    private final ContainerTypeRepository containerTypeRepository;
    private final ContainerRepository containerRepository;
    private final Validator validator;
    private final MessageService messageService;
    private final ContainerTypeMapper containerTypeMapper;
    private final ContainerValidationService containerValidationService;

    public List<ContainerTypeResponse> getAllContainerTypes() {
        return containerTypeRepository.findAll().stream()
                .map(containerTypeMapper::toResponse)
                .toList();
    }

    public ContainerTypeResponse getContainerTypeById(final Long containerTypeId) {
        return containerTypeRepository.findById(containerTypeId)
                .map(containerTypeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_ID, containerTypeId)));
    }

    @Transactional
    public ContainerTypeResponse createContainerType(final ContainerTypeRequest request) {
        validator.validateUniqueness(request.name(), containerTypeRepository::findByName,
                messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NAME_EXISTS, request.name()));

        ContainerTypeEntity containerType = containerTypeMapper.toEntity(request);
        return containerTypeMapper.toResponse(containerTypeRepository.save(containerType));
    }

    @Transactional
    public ContainerTypeResponse updateContainerType(final Long containerTypeId, final ContainerTypeRequest request) {
        ContainerTypeEntity containerType = containerTypeRepository.findById(containerTypeId)
                .orElseThrow(() -> new EntityNotFoundException(
                                messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_ID, containerTypeId)));

        updateNameIfProvided(request, containerType);
        updateDescriptionIfProvided(request, containerType);
        updateCapacityIfProvided(request, containerType);

        validateContainerSize(containerType);

        return containerTypeMapper.toResponse(containerTypeRepository.save(containerType));
    }

    @Transactional
    public void deleteContainerTypeById(final Long containerTypeId) {
        if (!containerTypeRepository.existsById(containerTypeId)) {
            throw new EntityNotFoundException(
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_ID, containerTypeId));
        }
        if (containerRepository.existsByContainerType_Id(containerTypeId)) {
            throw new IllegalStateException(
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_CONTAINERS_STILL_EXIST, containerTypeId));
        }

        containerTypeRepository.deleteById(containerTypeId);
    }

    private void validateContainerSize(final ContainerTypeEntity containerType) {
        List<ContainerEntity> containers = getAllContainersByContainerTypeId(containerType.getId());

        containers.forEach(containerValidationService::validateIsContainerFitIntoInventory);
        containers.forEach(containerValidationService::validateIsContentFitIntoContainerInventory);
    }

    private void updateCapacityIfProvided(final ContainerTypeRequest request, final ContainerTypeEntity entity) {
        if (!validator.isNullOrEmpty(request.capacity())) {
            entity.setCapacity(request.capacity());
        }
    }

    private List<ContainerEntity> getAllContainersByContainerTypeId(final Long containerTypeId) {
        return containerRepository.findAllByContainerType_IdAndRemovedFalse(containerTypeId);
    }

    private void updateDescriptionIfProvided(final ContainerTypeRequest request, final ContainerTypeEntity entity) {
        if (!validator.isNullOrEmpty(request.description())) {
            entity.setDescription(request.description());
        }
    }

    private void updateNameIfProvided(final ContainerTypeRequest request, final ContainerTypeEntity entity) {
        if (!validator.isNullOrEmpty(request.name())) {
            validator.validateUniqueness(request.name(), containerTypeRepository::findByName,
                    messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NAME_EXISTS, request.name()));
            entity.setName(request.name());
        }
    }
}
