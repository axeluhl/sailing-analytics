package com.sap.sailing.domain.smartphoneadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.SmartphoneImeiIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.impl.SmartphoneImeiIdentifierJsonSerializer;

public class SmartphoneImeiHandlerImpl implements DeviceIdentifierPersistenceHandler, DeviceIdentifierJsonSerializationHandler {
//    private static final Logger logger = Logger.getLogger(SmartphoneImeiHandlerImpl.class.getName());

    private final SmartphoneImeiIdentifierJsonDeserializer deserializer = new SmartphoneImeiIdentifierJsonDeserializer();
    private final SmartphoneImeiIdentifierJsonSerializer serializer = new SmartphoneImeiIdentifierJsonSerializer();
    
    
    private SmartphoneImeiIdentifier castIdentifier(DeviceIdentifier identifier) throws IllegalArgumentException {
        if (! (identifier instanceof SmartphoneImeiIdentifier)) throw new IllegalArgumentException("Expected a SmartphoneImeiIdentifier, got instead: " + identifier);
        return (SmartphoneImeiIdentifier) identifier;
    }

    @Override
    public Object store(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException {
        return castIdentifier(deviceIdentifier).getImei();
    }

    @Override
    public DeviceIdentifier load(Object input) {
        return new SmartphoneImeiIdentifier((String) input);
    }

    @Override
    public JSONObject serialize(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException {
        return serializer.serialize(castIdentifier(deviceIdentifier));
    }

    @Override
    public DeviceIdentifier deserialize(JSONObject json) throws JsonDeserializationException {
        return deserializer.deserialize(json);
    }
}
