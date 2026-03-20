package com.cherry.wms_lite.model.request.container;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContainerRequest(
        @NotBlank(message = "Serial number is required", groups = OnCreate.class)
        String serialNumber,

        @NotBlank(message = "Container type name is required", groups = OnCreate.class)
        String containerTypeName,

        @NotNull(message = "Status is required", groups = OnCreate.class)
        ContainerStatusEnum status,

        @NotBlank(message = "Location name is required", groups = OnCreate.class)
        String locationName,

        @NotNull(message = "Location type is required", groups = OnCreate.class)
        LocationTypeEnum locationTypeEnum
) {}
