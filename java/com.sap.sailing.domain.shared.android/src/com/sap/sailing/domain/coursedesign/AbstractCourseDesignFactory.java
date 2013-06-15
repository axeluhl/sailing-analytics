package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;


public abstract class AbstractCourseDesignFactory implements CourseDesignFactory{
    
    @Override
    public abstract CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Integer windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime);
    
    protected Position getPositionForGivenPointDistanceAndBearing(Position givenPoint, int ditanceInMeters,
            float bearingInDegree) {
        double bearingInRad = Math.toRadians(bearingInDegree);
        final int earthRadiusInMeters = 6371000;
        double lat = Math.asin(Math.sin(givenPoint.getLatDeg()) * Math.cos(ditanceInMeters / earthRadiusInMeters)
                + Math.cos(givenPoint.getLatDeg()) * Math.sin(ditanceInMeters / earthRadiusInMeters)
                * Math.cos(bearingInRad));
        double lon = givenPoint.getLngDeg()
                + Math.atan2(
                        Math.sin(bearingInRad) * Math.sin(ditanceInMeters / earthRadiusInMeters)
                                * Math.cos(givenPoint.getLatDeg()),
                        Math.cos(ditanceInMeters / earthRadiusInMeters) - Math.sin(givenPoint.getLatDeg()) * Math.sin(lat));
        return new DegreePosition(lat, lon);
    }
}
