package com.cherry.wms_lite.model.response;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public record GetContainerResponse(Long id,
                                   String containerType,
                                   String containerSerialNumber,
                                   Instant createdAt,
                                   Enum<ContainerStatusEnum> status)
{ }
