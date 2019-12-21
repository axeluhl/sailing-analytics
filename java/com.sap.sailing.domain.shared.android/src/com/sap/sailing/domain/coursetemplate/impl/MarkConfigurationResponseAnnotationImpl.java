package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationResponseAnnotation;

public class MarkConfigurationResponseAnnotationImpl implements MarkConfigurationResponseAnnotation {
    private final Position lastKnownPosition;
    private final DeviceIdentifier currentTrackingDevice;
    
    public MarkConfigurationResponseAnnotationImpl(Position lastKnownPosition, DeviceIdentifier currentTrackingDevice) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.currentTrackingDevice = currentTrackingDevice;
    }

    @Override
    public Position getLastKnownPosition() {
        return lastKnownPosition;
    }

    @Override
    public DeviceIdentifier getCurrentTrackingDevice() {
        return currentTrackingDevice;
    }
}
