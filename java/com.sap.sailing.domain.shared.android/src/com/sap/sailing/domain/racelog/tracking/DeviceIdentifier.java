package com.sap.sailing.domain.racelog.tracking;

import java.io.Serializable;

/**
 * Identifies any kind of tracking device. Should be implemented accordingly for different tracking
 * adapters, e.g. for smartphones, Igtimi trackers etc.
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
