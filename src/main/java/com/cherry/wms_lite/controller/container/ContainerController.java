package com.cherry.wms_lite.controller.container;

import com.cherry.wms_lite.model.response.GetContainerResponse;
import com.cherry.wms_lite.service.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/container")
@RequiredArgsConstructor
public class ContainerController {
    private final ContainerService containerService;

    @GetMapping("/all")
    public List<GetContainerResponse> getAllContainers() {
        return containerService.getAllContainers();
    }
}
