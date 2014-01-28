package com.sap.sailing.domain.smartphoneadapter;

import com.sap.sailing.domain.devices.DeviceIdentifier;

public class DeviceNotMappedException extends Exception {
    private static final long serialVersionUID = 7343339139579578931L;

    private final DeviceIdentifier device;

    public DeviceNotMappedException(DeviceIdentifier device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "Device is not mapped to race and competitor: " + device;
    }
}
