package com.cherry.wms_lite.model.request.storage_location;

import com.cherry.wms_lite.model.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;

public record StorageLocationRequest(
        @NotBlank(message = "Name is required", groups = OnCreate.class)
        String name,
        String description
) {
}
