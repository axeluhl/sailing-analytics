package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class SmartphoneImeiIdentifierJsonDeserializer implements JsonDeserializer<SmartphoneImeiIdentifier> {

    public static final String FIELD_IMEI = "imei";

    @Override
    public SmartphoneImeiIdentifier deserialize(JSONObject object) throws JsonDeserializationException {
        String imei = (String) object.get(FIELD_IMEI);
        return (SmartphoneImeiIdentifier) new SmartphoneImeiIdentifier(imei).resolve(null);
    }
}
