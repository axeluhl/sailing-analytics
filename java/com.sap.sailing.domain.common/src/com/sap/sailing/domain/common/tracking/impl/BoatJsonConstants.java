package com.sap.sailing.domain.common.tracking.impl;

/**
 * Used to assemble a compatible JSON string by themselves, using the constants from here.The field identified by this
 * name tells the fully-qualified class name from which the value of the field identified by {@link #FIELD_ID} has been
 * created. When deserializing, the ID needs to be re-constructed as an instance of that type.
 */
public class BoatJsonConstants {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAIL_ID = "sailID";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_BOAT_CLASS_NAME = "boatClassName";
}
