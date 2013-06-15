package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Position;


public interface CourseDesignFactory {
    CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Integer windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime);
}
