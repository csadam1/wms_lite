package com.cherry.wms_lite.model.response;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetContainerResponse {
    private String containerType;
    private String containerSerialNumber;
    private Instant createdAt;
    private Enum<ContainerStatusEnum> status;
}
