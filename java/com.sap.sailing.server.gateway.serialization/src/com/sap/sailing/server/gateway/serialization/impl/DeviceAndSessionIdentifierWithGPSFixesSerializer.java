package com.sap.sailing.server.gateway.serialization.impl;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DeviceAndSessionIdentifierWithGPSFixesSerializer<D extends DeviceIdentifier, F extends GPSFix> implements
        JsonSerializer<Triple<D, UUID, List<F>>> {
    
    private final JsonSerializer<D> deviceSerializer;
    private  JsonSerializer<F> fixSerializer;

    public DeviceAndSessionIdentifierWithGPSFixesSerializer(JsonSerializer<D> deviceSerializer,
            JsonSerializer<F> fixSerializer) {
        this.deviceSerializer = deviceSerializer;
        this.fixSerializer = fixSerializer;
    }

    @Override
    public JSONObject serialize(Triple<D, UUID, List<F>> data) {
        JSONObject result = new JSONObject();
        
        JSONObject deviceIdJson = deviceSerializer.serialize(data.getA());
        JSONArray fixesJson = new JSONArray();
        for (F fix : data.getC()) {
            fixesJson.add(fixSerializer.serialize(fix));
        }
        
        result.put(DeviceAndSessionIdentifierWithGPSFixesDeserializer.FIELD_DEVICE_ID, deviceIdJson);
        if (data.getB() != null) {
            result.put(DeviceAndSessionIdentifierWithGPSFixesDeserializer.FIELD_SESSION_UUID, data.getB().toString());            
        }
        result.put(DeviceAndSessionIdentifierWithGPSFixesDeserializer.FIELD_FIXES, fixesJson);

        return result;
    }

}
