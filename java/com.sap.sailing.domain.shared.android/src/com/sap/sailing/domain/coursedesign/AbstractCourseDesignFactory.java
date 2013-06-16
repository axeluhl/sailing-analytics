package com.sap.sailing.domain.coursedesign;

import java.util.Set;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;

public abstract class AbstractCourseDesignFactory implements CourseDesignFactory {
    protected CourseDesign product;
    private final int ANGLE_OF_START_LINE_TO_WIND = 270;
    private final Distance REFERENCE_POINT_DISTANCE_FROM_START_LINE = new NauticalMileDistance(0.05);
    
    abstract protected Set<PositionedMark> computeDesignSpecificMarks(Position startBoatPosition, Double windSpeed, Bearing windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime);

    @Override
    public abstract CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime);
    
    protected void initializeCourseDesign(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime){
        this.product.setStartBoatPosition(startBoatPosition);
        this.product.setWindSpeed(windSpeed);
        this.product.setWindDirection(windDirection);
        setPinEnd(boatClass, startBoatPosition, windDirection);
        setReferencePoint(boatClass, startBoatPosition, windDirection);
    }
    
    protected void finalizeCourseDesign(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime){
        this.product.getCourseDesignSpecificMarks().addAll(this.computeDesignSpecificMarks(startBoatPosition, windSpeed, windDirection, boatClass, courseLayout, numberOfRounds, targetTime));
    }

    protected void setPinEnd(BoatClassType boatClass, Position startBoatPosition, Bearing windDirection) {
        
        PositionedMark pinEnd = new PositionedMarkImpl("start pin", getPositionForGivenPointDistanceAndBearing(
                startBoatPosition, boatClass.getStartLineLength(),
                windDirection.add(new DegreeBearingImpl(ANGLE_OF_START_LINE_TO_WIND))));
        product.setPinEnd(pinEnd);
    }

    protected void setReferencePoint(BoatClassType boatClass, Position startBoatPosition, Bearing windDirection) {
        Position startLineMid = getPositionForGivenPointDistanceAndBearing(
                startBoatPosition, boatClass.getStartLineLength().scale(0.5),
                windDirection.add(new DegreeBearingImpl(ANGLE_OF_START_LINE_TO_WIND)));
        Position referencePoint = getPositionForGivenPointDistanceAndBearing(
                startLineMid, REFERENCE_POINT_DISTANCE_FROM_START_LINE,
                windDirection);
        product.setReferencePoint(referencePoint);
    }

    protected Position getPositionForGivenPointDistanceAndBearing(Position givenPoint, Distance distance,
            Bearing windDirection) {
        final double earthRadiusInMeters = 6371000.0;
        double brng = windDirection.getRadians();
        double lat1 = givenPoint.getLatRad();
        double lon1 = givenPoint.getLngRad();
        double dist = distance.getMeters() / earthRadiusInMeters;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
                        Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new DegreePosition(Math.toDegrees(lat2), Math.toDegrees(lon2));

        /*
         * LatLon.prototype.destinationPoint = function(brng, dist) { dist = typeof(dist)=='number' ? dist :
         * typeof(dist)=='string' && dist.trim()!='' ? +dist : NaN; dist = dist/this._radius; // convert dist to angular
         * distance in radians brng = brng.toRad(); // var lat1 = this._lat.toRad(), lon1 = this._lon.toRad();
         * 
         * var lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) ); var
         * lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1),
         * Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2)); lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI; // normalise
         * to -180..+180º
         * 
         * return new LatLon(lat2.toDeg(), lon2.toDeg()); }
         */
    }
}
