package com.sap.sailing.server.gateway.serialization.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.PlaceHolderDeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.PlaceHolderDeviceIdentifierJsonHandler;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.SingleTypeBasedServiceFinderImpl;

public class DeviceIdentifierJsonSerializer implements JsonSerializer<DeviceIdentifier> {
    private static final Logger logger = Logger.getLogger(DeviceIdentifierJsonSerializer.class.getName());
    
    private final TypeBasedServiceFinder<DeviceIdentifierJsonHandler> serviceFinder;
    
    /**
     * Create serializer for a single device type, with additional fallback to {@link PlaceHolderDeviceIdentifier}.
     */
    public static DeviceIdentifierJsonSerializer create(DeviceIdentifierJsonHandler singleHandler, String type) {
        return new DeviceIdentifierJsonSerializer(new SingleTypeBasedServiceFinderImpl<DeviceIdentifierJsonHandler>(
                singleHandler, new PlaceHolderDeviceIdentifierJsonHandler(),
                type));
    }
    
    public DeviceIdentifierJsonSerializer(TypeBasedServiceFinder<DeviceIdentifierJsonHandler> serviceFinder) {
        this.serviceFinder = serviceFinder;
    }

    @Override
    public JSONObject serialize(DeviceIdentifier object) {
        JSONObject result = new JSONObject();
        DeviceIdentifierJsonHandler handler = serviceFinder.findService(object.getIdentifierType());
        Util.Pair<String, ? extends Object> pair;
        try {
            pair = handler.serialize(object);
            result.put(DeviceIdentifierJsonDeserializer.FIELD_DEVICE_TYPE, pair.getA());
            result.put(DeviceIdentifierJsonDeserializer.FIELD_DEVICE_ID, pair.getB());
            result.put(DeviceIdentifierJsonDeserializer.FIELD_STRING_REPRESENTATION, object.getStringRepresentation());
            return result;
        } catch (TransformationException e) {
            logger.log(Level.WARNING, "Could not serialize device identifier, consider adding a fallback serialization handler");
            e.printStackTrace();
        }
        return null;
    }

}
