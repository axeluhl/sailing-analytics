package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.coursetemplate.TrackingDeviceBasedPositioning;

public class TrackingDeviceBasedPositioningImpl implements TrackingDeviceBasedPositioning {
    private final DeviceIdentifier deviceIdentifier;

    public TrackingDeviceBasedPositioningImpl(DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }
}
