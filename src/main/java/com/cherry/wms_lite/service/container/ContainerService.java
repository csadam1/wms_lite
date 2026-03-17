package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.response.GetContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerService {
    private final ContainerRepository repository;

    public List<GetContainerResponse> getAllContainers() {
        return repository
                .findAll()
                .stream()
                .map(this::mapToGetContainerResponse)
                .toList();
    }

    public GetContainerResponse getContainerById(final Long containerId) {
        return repository.findById(containerId)
                .map(this::mapToGetContainerResponse)
                .orElseThrow(() -> new EntityNotFoundException("Container not found: " + containerId));
    }

    private GetContainerResponse mapToGetContainerResponse(final ContainerEntity containerEntity) {
        return new GetContainerResponse(
                containerEntity.getId(),
                containerEntity.getContainerType().getName(),
                containerEntity.getSerialNumber(),
                containerEntity.getCreatedAt(),
                containerEntity.getStatus()
        );
    }
}
