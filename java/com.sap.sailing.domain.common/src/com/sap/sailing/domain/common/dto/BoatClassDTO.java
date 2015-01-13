package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class BoatClassDTO extends NamedDTO implements Serializable {
    private static final long serialVersionUID = 1981789833769906676L;
    
    /**
     * A default boat class name; can be used, e.g., in regatta configuration "templates" which later
     * are used to configure several different regattas for different boat classes.
     */
    public static final String DEFAULT_NAME = "Default";
    
    private double hullLengthInMeters;
    private String displayName;
    
    BoatClassDTO() {}

    public BoatClassDTO(String name, String displayName, double hullLengthInMeters) {
        super(name);
        this.hullLengthInMeters = hullLengthInMeters;
    }

    public BoatClassDTO(String name, double hullLengthInMeters) {
        super(name);
        this.hullLengthInMeters = hullLengthInMeters;
        this.displayName = null;
    }

    public double getHullLengthInMeters() {
        return hullLengthInMeters;
    }

    public String getDisplayName() {
        return displayName;
    }
}
