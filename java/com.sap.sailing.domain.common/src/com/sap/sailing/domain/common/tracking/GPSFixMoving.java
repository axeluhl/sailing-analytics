package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Moving;
import com.sap.sse.common.Bearing;

public interface GPSFixMoving extends GPSFix, Moving {
    /**
     * Some sensors have a compass or IMU built in that can deliver a true heading. This usually requires a fixed mount
     * that guarantees a well-defined orientation of the sensor relative to the object it tracks.
     * 
     * @return a true (free of magnetic declination) heading of the object tracked, e.g., for a boat the true direction
     *         the bow points to; or {@code null} if the sensor did not deliver this attribute
     */
    Bearing getOptionalTrueHeading();
}
