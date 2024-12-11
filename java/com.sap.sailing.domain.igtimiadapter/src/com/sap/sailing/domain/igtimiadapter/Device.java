package com.sap.sailing.domain.igtimiadapter;

import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface Device extends HasId, WithQualifiedObjectIdentifier {
    /**
     * @return a string identifying the device, such as "AA-AA-AAAA"
     */
    String getSerialNumber();

    String getServiceTag();

    Iterable<Permission> getPermissions();
}
