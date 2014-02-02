package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.impl.SmartphoneImeiIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SmartphoneImeiIdentifierJsonSerializer implements JsonSerializer<SmartphoneImeiIdentifier> {

    @Override
    public JSONObject serialize(SmartphoneImeiIdentifier identifier) {
        JSONObject object = new JSONObject();

        object.put(SmartphoneImeiIdentifierJsonDeserializer.FIELD_IMEI, identifier.getImei());
        return object;
    }
}
