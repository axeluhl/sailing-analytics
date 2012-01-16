package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.common.Bearing;

public class KnotSpeedWithBearingImpl extends AbstractSpeedWithBearingImpl {
    private final double speedInKnots;
    
    public KnotSpeedWithBearingImpl(double speedInKnots, Bearing bearing) {
        super(bearing);
        this.speedInKnots = speedInKnots;
    }
    
    @Override
    public double getKnots() {
        return speedInKnots;
    }

}
