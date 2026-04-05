package com.cherry.wms_lite.model.request.container_type;

import com.cherry.wms_lite.model.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContainerTypeRequest(
        @NotBlank(message = "Name is required", groups = OnCreate.class)
        String name,
        String description,
        @NotNull(message = "Capacity is required", groups = OnCreate.class)
        BigDecimal capacity
) {
}
