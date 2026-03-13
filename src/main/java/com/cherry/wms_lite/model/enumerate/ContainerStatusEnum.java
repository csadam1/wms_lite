package com.cherry.wms_lite.model.enumerate;

import lombok.Getter;

@Getter
public enum ContainerStatusEnum {
    OPEN("Open"),
    CLOSED("Closed"),
    BLOCKED("Blocked");

    private final String displayName;

    ContainerStatusEnum(final String displayName) {
        this.displayName = displayName;
    }
}
