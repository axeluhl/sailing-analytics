package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Distance;

public class NauticalMileDistance extends AbstractDistance {
    private final double nauticalMiles;
    
    public NauticalMileDistance(double nauticalMiles) {
        super();
        this.nauticalMiles = nauticalMiles;
    }

    @Override
    public double getNauticalMiles() {
        return nauticalMiles;
    }

    @Override
    public double getCentralAngleDeg() {
        return getGeographicalMiles() / 60.;
    }

    @Override
    public Distance scale(double factor) {
        return new NauticalMileDistance(factor * nauticalMiles);
    }

}
