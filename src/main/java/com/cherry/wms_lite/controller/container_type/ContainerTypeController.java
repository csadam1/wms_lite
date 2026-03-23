package com.cherry.wms_lite.controller.container_type;

import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import com.cherry.wms_lite.model.validation.OnCreate;
import com.cherry.wms_lite.model.validation.OnUpdate;
import com.cherry.wms_lite.service.container_type.ContainerTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/containerType")
@RequiredArgsConstructor
public class ContainerTypeController {
    private final ContainerTypeService containerTypeService;

    @GetMapping("/all")
    public List<ContainerTypeResponse> getAllContainerTypes() {
        return containerTypeService.getAllContainerTypes();
    }

    @GetMapping("/{id}")
    public ContainerTypeResponse getContainerTypeById(@PathVariable(name = "id") final Long containerTypeId) {
        return containerTypeService.getContainerTypeById(containerTypeId);
    }

    @PostMapping
    public ContainerTypeResponse createContainerType(
            @Validated(OnCreate.class) @RequestBody final ContainerTypeRequest request)
    {
        return containerTypeService.createContainerType(request);
    }

    @PutMapping("/{id}")
    public ContainerTypeResponse updateContainerType(@PathVariable(name = "id") final Long containerTypeId,
                                                     @Validated(OnUpdate.class) @RequestBody
                                                     final ContainerTypeRequest request)
    {
        return containerTypeService.updateContainerType(containerTypeId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeContainerTypeById(@PathVariable(name = "id") final Long containerTypeId) {
        containerTypeService.removeContainerTypeById(containerTypeId);
        return ResponseEntity.noContent().build();
    }
}
