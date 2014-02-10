package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;

public interface DeviceIdentifier extends IsManagedBySharedDomainFactory {
    /**
     * This type identifier is used to determine what adapter shall be used to resolve this device identifier. For
     * example, the smartphone adapter is used to resolve smartphone identifiers, while in future an Igtimi adapter
     * might be used to resolve Igtimi identifiers.
     * 
     * The string is used to look up OSGi services such as {@link com.sap.sailing.server.devices.DeviceMapper}s and
     * {@link com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler}s.
     * 
     * The reason for this design choice is that in future, third parties could easily write their own adapter and only
     * have to register new OSGi service, and not touch the SAP Sailing Analytics code.
     * 
     * @return
     */
    String getIdentifierType();

}
