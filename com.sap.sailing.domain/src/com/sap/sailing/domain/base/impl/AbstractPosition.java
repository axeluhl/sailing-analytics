package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;

public class AbstractPosition implements Position {
    public int hashCode() {
        return (int) (4711. * getLngRad() * getLatRad());
    }
    
    public boolean equals(Object o) {
        return o instanceof Position &&
               getLatRad() == ((Position) o).getLatRad() &&
               getLngRad() == ((Position) o).getLngRad();
    }
    
    @Override
    public double getLngRad() {
        return getLngDeg() / 180. * Math.PI;
    }

    @Override
    public double getLatRad() {
        return getLatDeg() / 180. * Math.PI;
    }

    @Override
    public double getLatDeg() {
        return getLatRad() / Math.PI * 180.;
    }

    @Override
    public double getLngDeg() {
        return getLngRad() / Math.PI * 180.;
    }

    @Override
    public double getCentralAngleRad(Position p) {
        return Math.acos(Math.sin(getLatRad()) * Math.sin(p.getLatRad())
                + Math.cos(getLatRad()) * Math.cos(p.getLatRad())
                * Math.cos(p.getLngRad() - getLngRad()));
    }

    @Override
    public Distance getDistance(Position p) {
        return new CentralAngleDistance(getCentralAngleRad(p));
    }

    @Override
    public Bearing getBearingGreatCircle(Position p) {
        double result = Math.atan2(Math.sin(p.getLngRad()-getLngRad()) * Math.cos(p.getLatRad()),
                Math.cos(getLatRad())*Math.sin(p.getLatRad()) -
                Math.sin(getLatRad())*Math.cos(p.getLatRad())*Math.cos(p.getLngRad()-getLngRad())) ;
        if (result < 0) {
            result = result + 2*Math.PI;
        }
        return new RadianBearingImpl(result);
    }

    @Override
    public Position translateRhumb(Bearing bearing, Distance distance) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Position translateGreatCircle(Bearing bearing, Distance distance) {
        double lat = Math.asin(Math.sin(getLatRad())
                * Math.cos(distance.getCentralAngleRad())
                + Math.cos(getLatRad())
                * Math.sin(distance.getCentralAngleRad())
                * Math.cos(bearing.getRadians()));
        double lng = getLngRad()
                + Math.atan2(
                        Math.sin(bearing.getRadians())
                                * Math.sin(distance.getCentralAngleRad())
                                * Math.cos(getLatRad()),
                        Math.cos(distance.getCentralAngleRad())
                                - Math.sin(getLatRad()) * Math.sin(lat));
        return new RadianPosition(lat, lng);
    }

    @Override
    public String toString() {
        return "("+getLatDeg()+","+getLngDeg()+")";
    }
}
