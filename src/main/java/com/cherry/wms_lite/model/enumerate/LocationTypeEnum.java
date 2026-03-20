package com.cherry.wms_lite.model.enumerate;

import lombok.Getter;

@Getter
public enum LocationTypeEnum {
    STORAGE_LOCATION("StorageLocation"),
    CONTAINER("Container");

    private final String displayName;

    LocationTypeEnum(final String displayName) {
        this.displayName = displayName;
    }
}
