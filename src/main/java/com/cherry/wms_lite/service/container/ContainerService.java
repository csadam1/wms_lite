package com.cherry.wms_lite.service.container;

import com.cherry.wms_lite.model.response.GetContainerResponse;
import com.cherry.wms_lite.repository.container.ContainerRepository;
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
                .map(containerEntity -> new GetContainerResponse(
                        containerEntity.getContainerType().getName(),
                        containerEntity.getSerialNumber(),
                        containerEntity.getCreatedAt(),
                        containerEntity.getStatus()
                ))
                .toList();
    }
}
