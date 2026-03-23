package com.cherry.wms_lite.controller.storage_location;

import com.cherry.wms_lite.model.request.storage_location.StorageLocationRequest;
import com.cherry.wms_lite.model.response.storage_location.StorageLocationResponse;
import com.cherry.wms_lite.model.validation.OnCreate;
import com.cherry.wms_lite.model.validation.OnUpdate;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storageLocation")
@RequiredArgsConstructor
public class StorageLocationController {
    private final StorageLocationService storageLocationService;

    @GetMapping("/all")
    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationService.getAllStorageLocations();
    }

    @GetMapping("/{id}")
    public StorageLocationResponse getStorageLocationById(@PathVariable(name = "id") final Long storageLocationId) {
        return storageLocationService.getStorageLocationById(storageLocationId);
    }

    @PostMapping
    public StorageLocationResponse createStorageLocation(
            @Validated(OnCreate.class) @RequestBody final StorageLocationRequest request)
    {
        return storageLocationService.createStorageLocation(request);
    }

    @PutMapping("/{id}")
    public StorageLocationResponse updateStorageLocation(@PathVariable(name = "id") final Long storageLocationId,
                                                         @Validated(OnUpdate.class) @RequestBody
                                                         final StorageLocationRequest request)
    {
        return storageLocationService.updateStorageLocation(storageLocationId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeStorageLocationById(@PathVariable(name = "id") final Long storageLocationId) {
        storageLocationService.deleteStorageLocationById(storageLocationId);
        return ResponseEntity.noContent().build();
    }
}
