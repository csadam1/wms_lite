package com.cherry.wms_lite.controller.container;

import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.model.validation.OnCreate;
import com.cherry.wms_lite.model.validation.OnUpdate;
import com.cherry.wms_lite.service.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/container")
@RequiredArgsConstructor
public class ContainerController {
    private final ContainerService containerService;

    @GetMapping("/all")
    public List<ContainerResponse> getAllContainers() {
        return containerService.getAllContainers();
    }

    @GetMapping("/{id}")
    public ContainerResponse getContainerById(@PathVariable(name = "id") final Long containerId) {
        return containerService.getContainerById(containerId);
    }

    @PostMapping
    public ContainerResponse createContainer(@Validated(OnCreate.class) @RequestBody final ContainerRequest request) {
        return containerService.createContainer(request);
    }

    @PutMapping("/{id}")
    public ContainerResponse updateContainer(@PathVariable(name = "id") final Long containerId,
                                             @Validated(OnUpdate.class) @RequestBody final ContainerRequest request)
    {
        return containerService.updateContainer(containerId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeContainerById(@PathVariable(name = "id") final Long containerId) {
        containerService.removeContainer(containerId);
        return ResponseEntity.noContent().build();
    }
}
