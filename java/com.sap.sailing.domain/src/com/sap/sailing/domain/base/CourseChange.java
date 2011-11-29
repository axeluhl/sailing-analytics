package com.sap.sailing.domain.base;

import com.sap.sailing.domain.tracking.GPSFixMoving;

/**
 * A course change happens at a position at a time and represents the original motion state with its {@link #getSpeed()}
 * and may change the course over ground as well as the speed over ground.
 * {@link SpeedWithBearing#applyCourseChange(CourseChange) Applying} this {@link CourseChange} object to a
 * {@link SpeedWithBearing} results in the {@link SpeedWithBearing} as it was after this course change.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface CourseChange extends GPSFixMoving {
    double getCourseChangeInDegrees();
    
    double getSpeedChangeInKnots();
}
