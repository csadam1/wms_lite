package com.cherry.wms_lite.model.request.item;

import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank(message = "Serial number is required", groups = OnCreate.class)
        String serialNumber,

        @NotBlank(message = "Material is required", groups = OnCreate.class)
        String material,

        @NotBlank(message = "Location name is required", groups = OnCreate.class)
        String locationName,

        @NotNull(message = "Location type is required", groups = OnCreate.class)
        LocationTypeEnum locationTypeEnum,

        @NotNull(message = "Quantity is required", groups = OnCreate.class)
        BigDecimal quantity
) {}
