package com.sap.sailing.domain.common;


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
