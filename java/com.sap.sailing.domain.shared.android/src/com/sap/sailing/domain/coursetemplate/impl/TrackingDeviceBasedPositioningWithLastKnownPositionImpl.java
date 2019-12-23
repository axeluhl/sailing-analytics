package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.TrackingDeviceBasedPositioningWithLastKnownPosition;

public class TrackingDeviceBasedPositioningWithLastKnownPositionImpl extends TrackingDeviceBasedPositioningImpl
implements TrackingDeviceBasedPositioningWithLastKnownPosition {
    private final Position lastKnownPosition;

    public TrackingDeviceBasedPositioningWithLastKnownPositionImpl(DeviceIdentifier deviceIdentifier, Position lastKnownPosition) {
        super(deviceIdentifier);
        this.lastKnownPosition = lastKnownPosition;
    }

    @Override
    public Position getLastKnownPosition() {
        return lastKnownPosition;
    }
}
