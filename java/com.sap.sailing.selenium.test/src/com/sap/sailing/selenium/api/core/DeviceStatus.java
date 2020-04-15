package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;

public class DeviceStatus extends JsonWrapper {
    public DeviceStatus(JSONObject json) {
        super(json);
    }

    public String getDeviceId() {
        return (String) ((JSONObject)get("deviceId")).get("id");
    }
    
    public GPSFixResponse getLastGPSFix() {
        JSONObject jsonObject = get("lastGPSFix");
        return jsonObject == null ? null : new GPSFixResponse(jsonObject);
    }
}