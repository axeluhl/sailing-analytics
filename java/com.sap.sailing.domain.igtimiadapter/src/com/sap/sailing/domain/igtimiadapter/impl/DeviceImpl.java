package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Device;

public class DeviceImpl extends HasIdImpl implements Device {
    private final String serialNumber;
    
    protected DeviceImpl(long id, String serialNumber) {
        super(id);
        this.serialNumber = serialNumber;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

}
