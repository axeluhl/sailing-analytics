package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.impl.DeviceImpl;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface Device extends HasId, WithQualifiedObjectIdentifier {
    /**
     * @return a string identifying the device, such as "AA-AA-AAAA"
     */
    String getSerialNumber();

    String getServiceTag();

    static Device create(long id, String serialNumber, String name, String serviceTag) {
        return new DeviceImpl(id, serialNumber, name, serviceTag);
    }

    static Device create(long id, String deviceSerialNumber) {
        return new DeviceImpl(id, deviceSerialNumber);
    }
}
