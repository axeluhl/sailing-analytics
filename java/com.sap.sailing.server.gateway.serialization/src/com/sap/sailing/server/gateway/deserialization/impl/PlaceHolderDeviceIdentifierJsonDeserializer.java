package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.tracking.PlaceHolderDeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierBaseJsonSerializer;

public class PlaceHolderDeviceIdentifierJsonDeserializer implements JsonDeserializer<PlaceHolderDeviceIdentifier> {
    @Override
    public PlaceHolderDeviceIdentifier deserialize(JSONObject object) throws JsonDeserializationException {
        String type = (String) object.get(TypeBasedJsonDeserializer.FIELD_TYPE);
        String stringRepresentation = (String) object.get(DeviceIdentifierBaseJsonSerializer.FIELD_STRING_REPRESENTATION);
        
        return new PlaceHolderDeviceIdentifier(type, stringRepresentation);
    }

}
