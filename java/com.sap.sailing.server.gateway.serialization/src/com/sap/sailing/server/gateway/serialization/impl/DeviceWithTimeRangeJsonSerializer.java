package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.shared.events.DeviceWithTimeRange;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DeviceWithTimeRangeJsonSerializer implements JsonSerializer<DeviceWithTimeRange> {
    public static final String FIELD_FROM = "FROM_MILLIS";
    public static final String FIELD_TO = "TO_MILLIS";
    
    private DeviceIdentifierJsonSerializer deviceIdentifierJsonSerializer;
    
    public DeviceWithTimeRangeJsonSerializer(DeviceIdentifierJsonSerializer deviceIdentifierJsonSerializer) {
        super();
        this.deviceIdentifierJsonSerializer = deviceIdentifierJsonSerializer;
    }

    @Override
    public JSONObject serialize(DeviceWithTimeRange object) {
        JSONObject result = deviceIdentifierJsonSerializer.serialize(object.getDevice());
        result.put(FIELD_FROM, object.getTimeRange().from().asMillis());
        result.put(FIELD_TO, object.getTimeRange().to().asMillis());
        return result;
    }
}
