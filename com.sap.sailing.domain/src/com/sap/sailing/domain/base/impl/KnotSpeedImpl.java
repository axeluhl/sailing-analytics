package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;

public class KnotSpeedImpl implements Speed {
    private final double speedInKnots;
    private final Bearing bearing;
    
    public KnotSpeedImpl(double speedInKnots, Bearing bearing) {
        super();
        this.speedInKnots = speedInKnots;
        this.bearing = bearing;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
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

    @Override
    public Distance travel(TimePoint t1, TimePoint t2) {
        return new NauticalMileDistance((t2.asMillis() - t1.asMillis()) / 1000. / 3600. * getKnots());
    }

}
