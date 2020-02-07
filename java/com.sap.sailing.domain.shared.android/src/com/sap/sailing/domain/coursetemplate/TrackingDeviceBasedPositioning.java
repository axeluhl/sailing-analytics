package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;

public interface TrackingDeviceBasedPositioning extends Positioning {
    DeviceIdentifier getDeviceIdentifier();
    default <T> T accept(PositioningVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
