package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.TimePoint;

/**
 * For a line such as a start or a finish line, tells the line's length at a given time point as well as its angle to a
 * true wind direction and the advantageous side in approaching direction as well as how much the advantageous side is
 * ahead.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface LineLengthAndAdvantage {
    TimePoint getTimePoint();

    Waypoint getWaypoint();

    Distance getLength();

    Bearing getAbsoluteAngleDifferenceToTrueWind();

    NauticalSide getAdvantageousSideWhileApproachingLine();

    Distance getAdvantage();
}
