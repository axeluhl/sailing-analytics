package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Position;

public class WindWardLeewardCourseDesignFactoryImpl extends AbstractCourseDesignFactory {

    @Override
    public CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Integer windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime) {
        CourseDesign windWardLeewardCourseDesign = new WindwardLeewardCourseDesignImpl();
        setPinEnd(windWardLeewardCourseDesign, boatClass, startBoatPosition, windDirection);
        return windWardLeewardCourseDesign;
    }

    private void setPinEnd(CourseDesign windWardLeewardCourseDesign, BoatClassType boatClass,
            Position startBoatPosition, Integer windDirection) {
        PositionedMark pinEnd = new PositionedMarkImpl("pinEnd", getPositionForGivenPointDistanceAndBearing(
                startBoatPosition, boatClass.getStartLineLengthInMeters(), windDirection));
        windWardLeewardCourseDesign.setPinEnd(pinEnd);
    }

}
