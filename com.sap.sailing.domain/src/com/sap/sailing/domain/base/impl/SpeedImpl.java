package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Seamile;
import com.sap.sailing.domain.base.Speed;

public class SpeedImpl implements Speed {
    private final double speedInKnots;
    private final double bearingDeg;
    
    public SpeedImpl(double speedInKnots, double bearingDeg) {
        super();
        this.speedInKnots = speedInKnots;
        this.bearingDeg = bearingDeg;
    }

    @Override
    public double getBearingDeg() {
        return bearingDeg;
    }
    
    @Override
    public double getBearingRad() {
        return getBearingDeg() / 180 * Math.PI;
    }

    @Override
    public double getKnots() {
        return speedInKnots;
    }

    @Override
    public double getMetersPerSecond() {
        return getKnots() * Seamile.AS_METERS / 3600;
    }

    @Override
    public double getKilometersPerHour() {
        return getKnots() * Seamile.AS_METERS / 1000;
    }

}
