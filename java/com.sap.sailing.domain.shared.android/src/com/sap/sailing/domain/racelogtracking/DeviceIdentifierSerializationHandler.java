package com.sap.sailing.domain.racelogtracking;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.Util;

/**
 * Base interface for handlers of serialization and deserialization of {@link DeviceIdentifier}s.
 * Additional information is passed around to enable fallback mechanisms (see {@link PlaceHolderDeviceIdentifier})
 * in case no appropriate handler for a {@code type} is registered.<p>
 * 
 * Handlers that implement child-interfaces (e.g. {@link DeviceIdentifierStringSerializationHandler} have to be
 * registered in the OSGi service registry for the respective child-interface.Also, the device identifier type has to be
 * added as a property to that service registration, as in this example:<p>
 * 
 * <pre>
 * Dictionary<String, String> properties = new Hashtable<String, String>();
 * properties.put(TypeBasedServiceFinder.TYPE, type);
 * context.registerService(DeviceIdentifierMongoHandler.class, new SmartphoneUUIDMongoHandler(), properties)}.
 * </pre><p>
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
     * @return A pair: <{@code device type}, {@code serialized identifier}>
     */
    Util.Pair<String, ? extends T> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException;
    
    /**
     * Deserialize the identifier. In case this is a fallback deserializer (e.g. for {@link PlaceHolderDeviceIdentifier}),
     * additional information such as {@code type} and {@code stringRepresentation} is passed in.<p>
     * The handler should check whether all necessary information is contained (specifically, if the {@code serialized}
     * object is not {@code null}, and otherwise throw an {@link TransformationException}.
     */
    DeviceIdentifier deserialize(T serialized, String type, String stringRepresentation) throws TransformationException;
}
