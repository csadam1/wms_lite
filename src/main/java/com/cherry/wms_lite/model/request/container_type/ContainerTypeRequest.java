package com.cherry.wms_lite.model.request.container_type;

import com.cherry.wms_lite.model.validation.OnCreate;
import com.cherry.wms_lite.model.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ContainerTypeRequest(
        @NotBlank(message = "Name is required", groups = OnCreate.class)
        String name,
        String description,
        @NotNull(message = "Capacity is required", groups = OnCreate.class)
        @Positive(message = "Capacity cannot be negative", groups = {OnCreate.class, OnUpdate.class})
        BigDecimal capacity
) {
}
