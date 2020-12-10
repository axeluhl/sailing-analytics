package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;

/**
 * Always has a non-{@code null} {@link #getDeviceIdentifier() device identifier}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TrackingDeviceBasedPositioning extends Positioning {
    DeviceIdentifier getDeviceIdentifier();
    default <T> T accept(PositioningVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
