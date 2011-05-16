package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Mile;

public class KilometersPerHourSpeedImpl extends AbstractSpeedImpl {
    private final double speedInKilometersPerHour;

    public KilometersPerHourSpeedImpl(double speedInKilometersPerHour, Bearing bearing) {
        super(bearing);
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
