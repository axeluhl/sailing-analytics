package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;

public class SmartphoneImeiIdentifierJsonDeserializer extends TypeBasedJsonDeserializer<SmartphoneImeiIdentifier> {
    public static final String FIELD_IMEI = "imei";

    @Override
    protected String getType() {
    	return SmartphoneImeiIdentifier.TYPE;
    }

    @Override
    protected SmartphoneImeiIdentifier deserializeAfterCheckingType(JSONObject object)
            throws JsonDeserializationException {
        String imei = (String) object.get(FIELD_IMEI);
        return (SmartphoneImeiIdentifier) new SmartphoneImeiIdentifier(imei);
    }
}
