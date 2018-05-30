package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;
import com.sap.sse.util.impl.UUIDHelper;

public class DeviceAndSessionIdentifierWithGPSFixesDeserializer
implements JsonDeserializer<Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>>> {

    public static final String FIELD_DEVICE = "device";
    public static final String FIELD_SESSION_UUID = "sessionId";
    public static final String FIELD_FIXES = "fixes";

    private final TypeBasedServiceFinder<GPSFixJsonHandler> fixServiceFinder;
    private final JsonDeserializer<DeviceIdentifier> deviceDeserializer;

    public DeviceAndSessionIdentifierWithGPSFixesDeserializer(
            TypeBasedServiceFinder<GPSFixJsonHandler> fixServiceFinder,
            JsonDeserializer<DeviceIdentifier> deviceDeserializer) {
        this.fixServiceFinder = fixServiceFinder;
        this.deviceDeserializer = deviceDeserializer;
    }

    @Override
    public Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>> deserialize(JSONObject object) throws JsonDeserializationException {
        JSONObject deviceIdObject = Helpers.toJSONObjectSafe(object.get(FIELD_DEVICE));
        DeviceIdentifier deviceId = deviceDeserializer.deserialize(deviceIdObject);

        Object sessionObject = object.get(FIELD_SESSION_UUID);
        Serializable sessionId = sessionObject == null ?
                null : UUIDHelper.tryUuidConversion(((Serializable) sessionObject));

        JSONArray fixesJson = Helpers.getNestedArraySafe(object, FIELD_FIXES);
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        for (Object fixObject : fixesJson) {
            JSONObject fixJson = (JSONObject) fixObject;
            GPSFix fix;
            try {
                fix = TypeBasedJsonDeserializer.deserialize(fixServiceFinder, fixJson);
            } catch (TransformationException e) {
                throw new JsonDeserializationException(e);
            }
            fixes.add(fix);
        }

        return new Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>>(deviceId, sessionId, fixes);
    }

}
