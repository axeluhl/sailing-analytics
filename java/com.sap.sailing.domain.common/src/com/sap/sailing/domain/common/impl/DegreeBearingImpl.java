package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.AbstractBearing;
import com.sap.sailing.domain.common.Bearing;


public class DegreeBearingImpl extends AbstractBearing implements Bearing {
    private final double bearingDeg;
    
    public DegreeBearingImpl(double bearingDeg) {
        super();
        this.bearingDeg = bearingDeg - 360*(int) (bearingDeg/360.);
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
