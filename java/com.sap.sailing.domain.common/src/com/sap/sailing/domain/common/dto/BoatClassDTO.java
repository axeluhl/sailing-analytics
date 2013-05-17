package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class BoatClassDTO extends NamedDTO implements Serializable {
    private static final long serialVersionUID = 1981789833769906676L;
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
