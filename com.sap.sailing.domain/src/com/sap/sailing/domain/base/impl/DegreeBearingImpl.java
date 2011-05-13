package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;

public class DegreeBearingImpl implements Bearing {
    private final double bearingDeg;
    
    public DegreeBearingImpl(double bearingDeg) {
        super();
        this.bearingDeg = bearingDeg;
    }

    @Override
    public double getDegrees() {
        return bearingDeg;
    }

    @Override
    public double getRadians() {
        return getDegrees() / 180. * Math.PI;
    }

}
