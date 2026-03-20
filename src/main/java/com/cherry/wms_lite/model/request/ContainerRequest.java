package com.cherry.wms_lite.model.request;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContainerRequest {
    @NotBlank(message = "Serial number is required", groups = OnCreate.class)
    private String serialNumber;

    @NotBlank(message = "Container type name is required", groups = OnCreate.class)
    private String containerTypeName;

    @NotNull(message = "Status is required", groups = OnCreate.class)
    private ContainerStatusEnum status;

    @NotBlank(message = "Location name is required", groups = OnCreate.class)
    private String locationName;

    @NotNull(message = "Location type is required", groups = OnCreate.class)
    private LocationTypeEnum locationTypeEnum;
}
