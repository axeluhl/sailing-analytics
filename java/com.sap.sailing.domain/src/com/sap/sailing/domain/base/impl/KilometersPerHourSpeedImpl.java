package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.Speed;

public class KilometersPerHourSpeedImpl extends AbstractSpeedImpl implements Speed {
    private final double speedInKilometersPerHour;
    
    public KilometersPerHourSpeedImpl(double speedInKilometersPerHour) {
        this.speedInKilometersPerHour = speedInKilometersPerHour;
    }
    
    @Override
    public double getKnots() {
        return getKilometersPerHour() * 1000. / Mile.METERS_PER_NAUTICAL_MILE;
    }

    @Override
    public double getMetersPerSecond() {
        return getKilometersPerHour() / 3.6;
    }

    @Override
    public double getKilometersPerHour() {
        return speedInKilometersPerHour;
    }
}
