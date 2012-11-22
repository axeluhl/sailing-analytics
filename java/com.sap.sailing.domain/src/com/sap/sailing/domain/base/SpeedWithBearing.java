package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;


public interface SpeedWithBearing extends Speed {
    Bearing getBearing();

    /**
     * Traveling at this speed starting at time <code>from</code> in position <code>pos</code> until time
     * </code>to</code>, how far have we traveled? If <code>to</code> is before </code>from</code>, the speed will be
     * applied in reverse.
     */
    Position travelTo(Position pos, TimePoint from, TimePoint to);

    /**
     * Computes the minimal (in terms of bearing change) course and speed change required to reach the
     * target speed and bearing specified.
     */
    CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing);

    SpeedWithBearing applyCourseChange(CourseChange courseChange);
}
