package com.cherry.wms_lite.common;

public final class ExceptionMessageKeys {
    private ExceptionMessageKeys() {}

    // Container Type
    public static final String CONTAINER_TYPE_NOT_FOUND_WITH_ID = "exception.container_type.not_found_with_id";
    public static final String CONTAINER_TYPE_NAME_EXISTS = "exception.container_type.name_exists";
    public static final String CONTAINERS_EXCEED_NEW_CAPACITY = "exception.container_type.containers_exceed_capacity";
    public static final String CONTAINER_TYPE_CONTAINERS_STILL_EXIST = "exception.container_type.containers_still_exist";

    // Container
    public static final String CONTAINER_SERIAL_EXISTS = "exception.container.serial_exists";
    public static final String CONTAINER_NOT_FOUND_WITH_ID = "exception.container.not_found_with_id";
    public static final String CONTAINER_NOT_FOUND_WITH_SERIAL = "exception.container.not_found_with_serial";
    public static final String CONTAINER_NOT_EMPTY = "exception.container.not_empty";
    public static final String CONTAINER_CAPACITY_EXCEEDED = "exception.container.capacity_exceeded";
    public static final String CONTAINER_TYPE_NOT_FOUND_WITH_NAME = "exception.container.type_not_found_with_name";
    public static final String CONTAINER_DOES_NOT_HAVE_VALID_STORAGE = "exception.container.does_not_have_valid_storage";
    public static final String CONTAINER_IS_NOT_ANY_IN_INVENTORY = "exception.container.is_not_in_any_inventory";

    // Storage Location
    public static final String STORAGE_LOCATION_NOT_FOUND_WITH_NAME = "exception.storage_location.not_found_with_name";
    public static final String STORAGE_LOCATION_NOT_FOUND_WITH_ID = "exception.storage_location.not_found_with_id";
    public static final String STORAGE_LOCATION_NAME_EXISTS = "exception.storage_location.name_exists";

    // Item
    public static final String ITEM_NOT_FOUND_WITH_ID = "exception.item.not_found_with_id";
    public static final String ITEM_DOES_NOT_HAVE_VALID_STORAGE = "exception.item.does_not_have_valid_storage";
    public static final String ITEM_SERIAL_NUMBER_EXISTS = "exception.item.serial_number_exists";

    // Location
    public static final String LOCATION_NAME_DOES_NOT_EXIST = "exception.location.name_does_not_exist";

    // Inventory
    public static final String PARENT_INVENTORY_IS_UNATTACHED = "exception.inventory.parent_inventory_is_unattached";
}
