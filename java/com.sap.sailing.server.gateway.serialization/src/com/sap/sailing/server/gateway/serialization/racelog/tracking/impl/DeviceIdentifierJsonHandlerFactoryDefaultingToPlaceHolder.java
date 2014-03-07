package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.SingleTypeBasedServiceFinderImpl;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.impl.PlaceHolderDeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class DeviceIdentifierJsonHandlerFactoryDefaultingToPlaceHolder extends
        SingleTypeBasedServiceFinderImpl<DeviceIdentifierJsonHandler> {
    private final DeviceIdentifierJsonHandler placeHolderHandler;
    

    public DeviceIdentifierJsonHandlerFactoryDefaultingToPlaceHolder(DeviceIdentifierJsonHandler service, String type) {
        super(service, type);
        final PlaceHolderDeviceIdentifierJsonDeserializer placeHolderDeserializer =
                new PlaceHolderDeviceIdentifierJsonDeserializer();
        final DeviceIdentifierBaseJsonSerializer<DeviceIdentifier> placeHolderSerializer =
                new DeviceIdentifierBaseJsonSerializer<DeviceIdentifier>() {
            @Override
            protected JSONObject furtherSerialize(DeviceIdentifier object, JSONObject result) {
                return result;
            }
        };
        
        placeHolderHandler = new DeviceIdentifierJsonHandler() {
            
            @Override
            public JSONObject transformForth(DeviceIdentifier object) throws TransformationException {
                return placeHolderSerializer.serialize(object);
            }
            
            @Override
            public DeviceIdentifier transformBack(JSONObject object) throws TransformationException {
                return placeHolderDeserializer.deserialize(object);
            }
        };
    }

    @Override
    public DeviceIdentifierJsonHandler findService(String type) {
        try {
            return super.findService(type);
        } catch (NoCorrespondingServiceRegisteredException e) {
            return placeHolderHandler;
        }
    }
}
