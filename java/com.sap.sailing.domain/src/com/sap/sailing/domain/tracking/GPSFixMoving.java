package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Moving;
import com.sap.sailing.domain.base.SpeedWithBearing;

public interface GPSFixMoving extends GPSFix, Moving {
    /**
     * Computes the minimal (in terms of bearing change) course and speed change required to reach the
     * target speed and bearing specified.
     */
    CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing);
}
