package com.cherry.wms_lite.model.response.container_type;

import java.math.BigDecimal;

public record ContainerTypeResponse(Long id,
                                    String name,
                                    String description,
                                    BigDecimal capacity)
{
}
