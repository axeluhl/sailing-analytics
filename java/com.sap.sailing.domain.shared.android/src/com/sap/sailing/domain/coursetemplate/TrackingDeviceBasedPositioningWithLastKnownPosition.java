package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.Position;

/**
 * Adds a last known position to this tracking device-based positioning request. The last known position
 * is only for information purposes and will have no influence on the actual positioning request.<p>
 * 
 * TODO remove this class; it doesn't seem to make sense to carry this information through
 */
public interface TrackingDeviceBasedPositioningWithLastKnownPosition extends TrackingDeviceBasedPositioning {
    Position getLastKnownPosition();

    @Override
    default <T> T accept(PositioningVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
