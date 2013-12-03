package com.sap.sailing.domain.igtimiadapter;

public interface Device extends HasId {
    /**
     * @return a string identifying the device, such as "AA-AA-AAAA"
     */
    String getSerialNumber();
}
