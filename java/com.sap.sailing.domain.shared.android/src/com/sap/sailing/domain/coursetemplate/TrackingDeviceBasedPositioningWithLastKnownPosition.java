package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.Position;

public interface TrackingDeviceBasedPositioningWithLastKnownPosition extends TrackingDeviceBasedPositioning {
    Position getLastKnownPosition();

    @Override
    default <T> T accept(PositioningVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
