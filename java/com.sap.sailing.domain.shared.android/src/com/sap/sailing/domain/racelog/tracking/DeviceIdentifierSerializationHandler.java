package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;

/**
 * Handles serialization and deserialization of {@link DeviceIdentifier}s.
 * Additional information is passed around to enable fallback mechanisms (see {@link PlaceHolderDeviceIdentifier})
 * in case no appropriate handler for a {@code type} is registered.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface DeviceIdentifierSerializationHandler<T> {
    /**
     * Serialize the identifier. Also returns additional information, in case this is a fallback
     * serializer, that actually returns some generic serializiation (e.g. {@link PlaceHolderDeviceIdentifier}).
     * 
     * @param deviceIdentifier
     * @return A pair consisting of the {@code serialized identifier} and the {@code device type}.
     */
    Pair<? extends T, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException;
    
    /**
     * Deserialize the identifier. In case this is a fallback deserializer (e.g. for {@link PlaceHolderDeviceIdentifier}),
     * additional information such as {@code type} and {@code stringRepresentation} is passed in.
     */
    DeviceIdentifier deserialize(T serialized, String type, String stringRepresentation) throws TransformationException;
}
