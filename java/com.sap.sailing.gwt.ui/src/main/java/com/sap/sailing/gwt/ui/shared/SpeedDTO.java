package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SpeedDTO implements IsSerializable {
    public double speedInKnots;

    public SpeedDTO() {}
    
    public SpeedDTO(double speedInKnots) {
        super();
        this.speedInKnots = speedInKnots;
    }
    
    @Override
    public String toString() {
        return ""+speedInKnots+"kn";
    }
}
