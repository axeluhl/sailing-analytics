package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class TrackingDeviceApi {

    private static final String BASE_PATH = "/api/v1/tracking_devices";

    public DeviceStatus getDeviceStatus(ApiContext ctx, UUID deviceUUID) {
        return new DeviceStatus(ctx.get(BASE_PATH + "/" + deviceUUID.toString()));
    }

    public class DeviceStatus extends JsonWrapper {
        private DeviceStatus(JSONObject json) {
            super(json);
        }

        public UUID getDeviceUUID() {
            return UUID.fromString(get("deviceUUID"));
        }
        
        public GPSFixResponse getLastFix() {
            JSONObject jsonObject = get("lastFix");
            return jsonObject == null ? null : new GPSFixResponse(jsonObject);
        }
    }
    
    public class GPSFixResponse extends JsonWrapper {
        private GPSFixResponse(JSONObject json) {
            super(json);
        }
        
        public long getTime() {
            return ((Number)get("unixtime")).longValue();
        }
    }
}
