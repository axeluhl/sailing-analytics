package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SpeedWithBearingDAO implements IsSerializable {
    public double speedInKnots;
    public double bearingInDegrees;

    public SpeedWithBearingDAO() {}
    
    public SpeedWithBearingDAO(double speedInKnots, double bearingInDegrees) {
        super();
        this.speedInKnots = speedInKnots;
        this.bearingInDegrees = bearingInDegrees;
    }
    
    @Override
    public String toString() {
        return ""+speedInKnots+"kn to "+bearingInDegrees+"&deg;";
    }
}
