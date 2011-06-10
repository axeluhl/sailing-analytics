package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;

public class RadianBearingImpl extends AbstractBearing implements Bearing {
    private final double bearingRad;
    
    public RadianBearingImpl(double bearingRad) {
        super();
        this.bearingRad = bearingRad;
    }

    @Override
    public double getDegrees() {
        return getRadians() / Math.PI * 180.;
    }

    @Override
    public double getRadians() {
        return bearingRad;
    }

}
