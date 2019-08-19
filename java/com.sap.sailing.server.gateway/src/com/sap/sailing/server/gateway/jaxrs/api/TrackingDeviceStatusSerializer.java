package com.sap.sailing.server.gateway.jaxrs.api;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;

public final class TrackingDeviceStatusSerializer implements JsonSerializer<TrackingDeviceStatus> {
    private final GPSFixJsonSerializer gpsFixSerializer = new GPSFixJsonSerializer();
    private final JsonSerializer<DeviceIdentifier> deviceIdSerializer;
    
    public TrackingDeviceStatusSerializer(JsonSerializer<DeviceIdentifier> deviceIdSerializer) {
        this.deviceIdSerializer = deviceIdSerializer;
    }
    
    @Override
    public JSONObject serialize(TrackingDeviceStatus deviceStatus) {
        JSONObject result = new JSONObject();
        result.put("deviceId", deviceIdSerializer.serialize(deviceStatus.getDeviceId()));
        if (deviceStatus.getLastGPSFix() != null) {
            result.put("lastGPSFix", gpsFixSerializer.serialize(deviceStatus.getLastGPSFix()));
        }
        return result;
    }
}