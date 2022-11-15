package com.sap.sailing.selenium.api.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.DeviceStatus;

public class TrackingDeviceApi {

    private static final String BASE_PATH = "/api/v1/tracking_devices";
    private static final Map<String, String> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());

    public DeviceStatus getDeviceStatus(ApiContext ctx, UUID deviceUUID) {
        return new DeviceStatus(ctx.post(BASE_PATH + "/" + deviceUUID.toString(),EMPTY_MAP));
    }
}
