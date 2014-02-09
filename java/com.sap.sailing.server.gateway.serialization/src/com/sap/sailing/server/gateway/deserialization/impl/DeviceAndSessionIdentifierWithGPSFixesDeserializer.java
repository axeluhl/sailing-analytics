package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;

public class DeviceAndSessionIdentifierWithGPSFixesDeserializer
    implements JsonDeserializer<Triple<DeviceIdentifier, Serializable, List<GPSFix>>> {

    public static final String FIELD_DEVICE_ID = "deviceId";
    public static final String FIELD_SESSION_UUID = "sessionId";
    public static final String FIELD_FIXES = "fixes";

    private final TypeBasedServiceFinder<GPSFixJsonSerializationHandler> fixServiceFinder;
    private final TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder;

    public DeviceAndSessionIdentifierWithGPSFixesDeserializer(
            TypeBasedServiceFinder<GPSFixJsonSerializationHandler> fixServiceFinder,
            TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder) {
        this.fixServiceFinder = fixServiceFinder;
        this.deviceServiceFinder = deviceServiceFinder;
    }

    @Override
    public Triple<DeviceIdentifier, Serializable, List<GPSFix>> deserialize(JSONObject object) throws JsonDeserializationException {
        JSONObject deviceIdJson = Helpers.getNestedObjectSafe(object, FIELD_DEVICE_ID);
        DeviceIdentifier deviceId = TypeBasedJsonDeserializer.deserialize(deviceServiceFinder, deviceIdJson);

        Serializable sessionId = Helpers.tryUuidConversion(((Serializable) object.get(FIELD_SESSION_UUID)));

        JSONArray fixesJson = Helpers.getNestedArraySafe(object, FIELD_FIXES);
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        for (Object fixObject : fixesJson) {
            JSONObject fixJson = (JSONObject) fixObject;
            GPSFix fix = TypeBasedJsonDeserializer.deserialize(fixServiceFinder, fixJson);
            fixes.add(fix);
        }

        return new Triple<DeviceIdentifier, Serializable, List<GPSFix>>(deviceId, sessionId, fixes);
    }

}
