package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;

/**
 * Identifies any kind of tracking device. Should be implemented accordingly for different tracking
 * adapters, e.g. for smartphones, Igtimi trackers etc.
 * @author Fredrik Teschke
 *
 */
public interface DeviceIdentifier extends IsManagedBySharedDomainFactory {
    /**
     * The returned {@link String} is used to look up corresponding services for serialization
     * and persistence.
     * 
     * The reason for this design choice is that in future, third parties could easily write their own adapter and only
     * have to register new OSGi service, and not touch the SAP Sailing Analytics code.
     */
    String getIdentifierType();

}
