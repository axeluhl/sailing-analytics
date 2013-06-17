package com.sap.sailing.racecommittee.app.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;


public interface CourseDesignFactory {
    CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime);

}
