package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.StoredDeviceIdentifierPositioning;

public class StoredDeviceIdentifierPositioningImpl extends AbstractPositioningImpl implements StoredDeviceIdentifierPositioning {

    public StoredDeviceIdentifierPositioningImpl(DeviceIdentifier deviceIdentifier, Position optionalCurrentPosition) {
        super(PositioningType.DEVICE, optionalCurrentPosition, deviceIdentifier);
    }
}
