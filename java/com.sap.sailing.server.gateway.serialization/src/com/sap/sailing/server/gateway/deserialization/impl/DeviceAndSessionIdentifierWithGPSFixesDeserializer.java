package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class DeviceAndSessionIdentifierWithGPSFixesDeserializer<D extends DeviceIdentifier, F extends GPSFix>
        implements JsonDeserializer<Triple<D, UUID, List<F>>> {

    public static final String FIELD_DEVICE_ID = "deviceId";
    public static final String FIELD_SESSION_UUID = "sessionId";
    public static final String FIELD_FIXES = "fixes";

    private final JsonDeserializer<D> deviceDeserializer;
    private final JsonDeserializer<F> fixDeserializer;

    public DeviceAndSessionIdentifierWithGPSFixesDeserializer(JsonDeserializer<D> deviceDeserializer,
            JsonDeserializer<F> fixDeserializer) {
        this.deviceDeserializer = deviceDeserializer;
        this.fixDeserializer = fixDeserializer;
    }

    @Override
    public Triple<D, UUID, List<F>> deserialize(JSONObject object) throws JsonDeserializationException {
        JSONObject deviceIdJson = Helpers.getNestedObjectSafe(object, FIELD_DEVICE_ID);
        D deviceId = deviceDeserializer.deserialize(deviceIdJson);

        String sessionIdString = (String) object.get(FIELD_SESSION_UUID);
        UUID sessionId = null;
        try {
            if (sessionIdString != null) {
                UUID.fromString((String) object.get(FIELD_SESSION_UUID));
            }
        } catch (IllegalArgumentException e) {
            throw new JsonDeserializationException(e);
        }

        JSONArray fixesJson = Helpers.getNestedArraySafe(object, FIELD_FIXES);
        List<F> fixes = new ArrayList<F>();
        for (Object fixJson : fixesJson) {
            fixes.add(fixDeserializer.deserialize((JSONObject) fixJson));
        }

        return new Triple<D, UUID, List<F>>(deviceId, sessionId, fixes);
    }

}
