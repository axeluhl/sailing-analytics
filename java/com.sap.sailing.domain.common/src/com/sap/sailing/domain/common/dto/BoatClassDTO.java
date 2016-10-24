package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.Distance;

public class BoatClassDTO extends NamedDTO implements Serializable, Comparable<BoatClassDTO> {
    private static final long serialVersionUID = 1981789833769906676L;
    
    /**
     * A default boat class name; can be used, e.g., in regatta configuration "templates" which later
     * are used to configure several different regattas for different boat classes.
     */
    public static final String DEFAULT_NAME = "Default";
    
    private Distance hullLength;
    private String displayName;
    
    BoatClassDTO() {}

    public BoatClassDTO(String name, String displayName, Distance hullLength) {
        super(name);
        this.hullLength = hullLength;
    }

    public BoatClassDTO(String name, Distance hullLength) {
        super(name);
        this.hullLength = hullLength;
        this.displayName = null;
    }

    public Distance getHullLength() {
        return hullLength;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public int compareTo(BoatClassDTO o) {
        return getName().compareToIgnoreCase(o.getName());
    }
}
