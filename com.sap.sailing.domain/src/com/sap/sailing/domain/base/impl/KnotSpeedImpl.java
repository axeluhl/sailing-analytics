package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Mile;

public class KnotSpeedImpl extends AbstractSpeedWithBearingImpl {
    private final double speedInKnots;
    
    public KnotSpeedImpl(double speedInKnots, Bearing bearing) {
        super(bearing);
        this.speedInKnots = speedInKnots;
    }

    @Override
    public double getKnots() {
        return speedInKnots;
    }

    @Override
    public double getMetersPerSecond() {
        return getKnots() * Mile.METERS_PER_SEA_MILE / 3600;
    }

    @Override
    public double getKilometersPerHour() {
        return getKnots() * Mile.METERS_PER_SEA_MILE / 1000;
    }

}
