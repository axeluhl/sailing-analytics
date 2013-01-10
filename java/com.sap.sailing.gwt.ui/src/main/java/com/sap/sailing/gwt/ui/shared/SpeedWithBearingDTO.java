package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SpeedWithBearingDTO implements IsSerializable {
    public double speedInKnots;
    public double bearingInDegrees;

    public SpeedWithBearingDTO() {}
    
    public SpeedWithBearingDTO(double speedInKnots, double bearingInDegrees) {
        super();
        this.speedInKnots = speedInKnots;
        this.bearingInDegrees = bearingInDegrees;
    }
    
    @Override
    public String toString() {
        return ""+speedInKnots+"kn to "+bearingInDegrees+"deg";
    }
}
