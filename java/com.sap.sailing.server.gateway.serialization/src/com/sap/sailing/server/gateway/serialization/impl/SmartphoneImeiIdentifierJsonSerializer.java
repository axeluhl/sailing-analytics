package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.impl.SmartphoneImeiIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SmartphoneImeiIdentifierJsonSerializer extends DeviceIdentifierBaseJsonSerializer<SmartphoneImeiIdentifier>
implements JsonSerializer<SmartphoneImeiIdentifier> {
    @Override
    protected JSONObject furtherSerialize(SmartphoneImeiIdentifier object, JSONObject result) {
        result.put(SmartphoneImeiIdentifierJsonDeserializer.FIELD_IMEI, object.getImei());
        return result;
    }
}
