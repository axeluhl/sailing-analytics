package com.sap.sailing.domain.common;

import java.io.Serializable;

/**
 * Identifies any kind of tracking device. Should be implemented accordingly for different tracking
 * adapters, e.g. for smartphones, Igtimi trackers etc.<p>
 * 
 * Device identifiers are integrated in such a way, that a third party wishing to use their own
 * types of device identifiers can do so without touching the existing code base. Instead, handlers
 * for serializing device identifiers can be registered via the OSGi service registry. When adding a
 * new device identifier, the following handlers <b>have to</b> be implemented and registered:
 * <ul>
 * <li>{@code DeviceIdentifierMongoHandler}</li>
 * </ul>
 * The following handlers are optional:
 * <ul>
 * <li>{@code DeviceIdentifierJsonHandler}:
 *     Enables managing such identifiers from a smartphone or tablet through the {@link RaceLog}.</li>
 * <li>{@code DeviceIdentifierStringSerializationHandler}:
 *     Enables device identifiers to be created and managed in the admin console.</li>
 * </ul>
 * </p>
 * 
 * Refer to {@link SmartphoneUUIDIdentifier} and the related handlers as an implementation example.
 * An example on how to register the handlers in the OSGi service registry is given in
 * {@link DeviceIdentifierSerializationHandler}.<p>
 * 
 * The handler is chosen based on the {@code identifier type} of the device identifier. This identifier
 * type should be unique, consider using a fully qualified class name.<p>
 * 
 * The {@link #getStringRepresentation() string representation} of a device identifier is used to generate
 * a {@link PlaceHolderDeviceIdentifier} in case an appropriate handler is missing.<p>
 * 
 * {@link Object#equals(Object)} and {@link Object#hashCode()} should be implemented so that multiple
 * deserializations of same device identifier (conceptually) result in objects that are be identified
 * as being equal through these two methods.
 * 
 * @author Fredrik Teschke
 *
 */
public interface DeviceIdentifier extends Serializable {
    /**
     * The returned {@link String} is used to look up corresponding services for serialization
     * and persistence.
     * 
     * The reason for this design choice is that in future, third parties could easily write their own adapter and only
     * have to register new OSGi service, and not touch the SAP Sailing Analytics code.
     */
    String getIdentifierType();
    
    /**
     * Create a string representation, that can identify this device.
     * The returned values should be unique for this identifier within its {@link #getIdentifierType() type},
     * but need not include the {@link #getIdentifierType() type} itself in the representation.
     */
    String getStringRepresentation();

}
