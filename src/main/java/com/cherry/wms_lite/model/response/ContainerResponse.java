package com.cherry.wms_lite.model.response;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;

import java.time.Instant;

public record ContainerResponse(Long id,
                                String containerType,
                                String containerSerialNumber,
                                Instant createdAt,
                                Enum<ContainerStatusEnum> status)
{ }
