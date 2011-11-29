package com.sap.sailing.domain.base;

import com.sap.sailing.domain.tracking.GPSFix;

/**
 * A course change happens at a position at a time and may change the course over ground as well as the speed over ground.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseChange extends GPSFix {
    double getCourseChangeInDegrees();
    
    double getSpeedChangeInKnots();
    
    /**
     * @return the side ({@link Tack#STARBOARD} or {@link Tack#PORT} or <code>null</code>) to which the direction changed
     */
    Tack to();
}
