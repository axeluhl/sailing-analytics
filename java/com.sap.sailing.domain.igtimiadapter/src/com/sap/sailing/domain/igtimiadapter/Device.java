package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.impl.DeviceImpl;
import com.sap.sse.common.Renamable;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface Device extends HasId, WithQualifiedObjectIdentifier, Renamable {
    /**
     * @return a string identifying the device, such as "AA-AA-AAAA"
     */
    String getSerialNumber();

    static Device create(long id, String serialNumber, String name) {
        return new DeviceImpl(id, serialNumber, name);
    }

    static Device create(long id, String deviceSerialNumber) {
        return new DeviceImpl(id, deviceSerialNumber);
    }
}
