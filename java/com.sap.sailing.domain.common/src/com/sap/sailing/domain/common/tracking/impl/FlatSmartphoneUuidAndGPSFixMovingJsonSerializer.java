package com.sap.sailing.domain.common.tracking.impl;

/**
 * So far only holds the constants required to assemble a JSON object properly. The challenge for this serializer is
 * that it shall run on Android where so far we don't have GPSFixMoving or any of its implementing classes as they are
 * part of <code>com.sap.sailing.comain</code> which is not available on Android. So the Android apps that want to
 * target <code>FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer</code> need to assemble a compatible JSON string by
 * themselves, using the constants from here.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class FlatSmartphoneUuidAndGPSFixMovingJsonSerializer {
    public static final String DEVICE_UUID = "deviceUuid";
    public static final String FIXES = "fixes";
    public static final String LON_DEG = "longitude";
    public static final String LAT_DEG = "latitude";
    public static final String TIME_MILLIS = "timestamp";
    public static final String SPEED_M_PER_S = "speed";
    public static final String BEARING_DEG = "course";
}
