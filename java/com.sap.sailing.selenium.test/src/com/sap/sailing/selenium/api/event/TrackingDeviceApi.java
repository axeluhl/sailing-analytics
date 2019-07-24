package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.DeviceStatus;

public class TrackingDeviceApi {

    private static final String BASE_PATH = "/api/v1/tracking_devices";

    public DeviceStatus getDeviceStatus(ApiContext ctx, UUID deviceUUID) {
        return new DeviceStatus(ctx.get(BASE_PATH + "/" + deviceUUID.toString()));
    }
}
