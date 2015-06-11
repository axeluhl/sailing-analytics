package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimpleWindDTO implements IsSerializable {
    private Double trueWindFromDeg;
    private Double trueWindSpeedInKnots;
    
    @SuppressWarnings("unused")
    private SimpleWindDTO() {
    }

    public SimpleWindDTO(Double trueWindFromDeg, Double trueWindSpeedInKnots) {
        super();
        this.trueWindFromDeg = trueWindFromDeg;
        this.trueWindSpeedInKnots = trueWindSpeedInKnots;
    }

    public Double getTrueWindFromDeg() {
        return trueWindFromDeg;
    }

    public Double getTrueWindSpeedInKnots() {
        return trueWindSpeedInKnots;
    }
}
