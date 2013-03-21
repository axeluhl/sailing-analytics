package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BoatClassDTO extends NamedDTO implements IsSerializable {
    private double hullLengthInMeters;
    
    BoatClassDTO() {}

    public BoatClassDTO(String name, double hullLengthInMeters) {
        super(name);
        this.hullLengthInMeters = hullLengthInMeters;
    }

    public double getHullLengthInMeters() {
        return hullLengthInMeters;
    }
}
