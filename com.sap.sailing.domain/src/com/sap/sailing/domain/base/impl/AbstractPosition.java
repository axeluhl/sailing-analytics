package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;

public class AbstractPosition implements Position {
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
    public Distance distance(Position p) {
        return new CentralAngleDistance(getCentralAngleRad(p));
    }

    @Override
    public double getBearingDeg(Position p) {
        return getBearingRad(p) / Math.PI * 180.;
    }

    @Override
    public double getBearingRad(Position p) {
        double result = Math.atan2(Math.sin(p.getLngRad()-getLngRad()) * Math.cos(p.getLatRad()),
                Math.cos(getLatRad())*Math.sin(p.getLatRad()) -
                Math.sin(getLatRad())*Math.cos(p.getLatRad())*Math.cos(p.getLngRad()-getLngRad())) ;
        if (result < 0) {
            result = result + 2*Math.PI;
        }
        return result;
    }

    @Override
    public Position translate(Bearing bearing, Distance distance) {
        // TODO Auto-generated method stub
        return null;
    }

}
