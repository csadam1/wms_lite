package com.cherry.wms_lite.model.response.item;

import java.math.BigDecimal;

public record ItemResponse (
        Long id,
        String serialNumber,
        String material,
        String storageLocationName,
        BigDecimal quantity
) {}
