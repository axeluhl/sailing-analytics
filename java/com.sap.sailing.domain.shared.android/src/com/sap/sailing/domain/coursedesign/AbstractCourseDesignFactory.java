package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;

public abstract class AbstractCourseDesignFactory implements CourseDesignFactory {
    protected CourseDesign product;

    @Override
    public abstract CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime);

    protected void setPinEnd(BoatClassType boatClass, Position startBoatPosition, Bearing windDirection) {
        PositionedMark pinEnd = new PositionedMarkImpl("pinEnd", getPositionForGivenPointDistanceAndBearing(
                startBoatPosition, boatClass.getStartLineLengthInMeters(),
                windDirection.add(new DegreeBearingImpl(270))));
        product.setPinEnd(pinEnd);
    }

    protected Position getPositionForGivenPointDistanceAndBearing(Position givenPoint, int ditanceInMeters,
            Bearing windDirection) {
        final double earthRadiusInMeters = 6371000.0;
        double brng = windDirection.getRadians();
        double lat1 = givenPoint.getLatRad();
        double lon1 = givenPoint.getLngRad();
        double dist = ditanceInMeters / earthRadiusInMeters;

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
