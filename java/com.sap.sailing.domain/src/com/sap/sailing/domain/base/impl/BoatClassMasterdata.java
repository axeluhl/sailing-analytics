package com.sap.sailing.domain.base.impl;

public enum BoatClassMasterdata {
    
    LASER_SB3 ( "Laser SB3", 6.15 ),
    EXTREME_40 ("Extreme 40", 12.2),
    J80 ("J/80", 8.0, "J80"),
    _49ER ("49er", 4.88),
    _29ER ("29er", 4.45),
    _505 ("505", 5.03),
    _420 ("420", 4.2),
    _470 ("470", 4.7);

    private final double hullLengthInMeter;

    private final String displayName;

    private final String[] alternativeNames;

    BoatClassMasterdata(String displayName, double hullLengthInMeter) {
        this.displayName = displayName;
        this.hullLengthInMeter = hullLengthInMeter;
        this.alternativeNames = new String[0];
    }

    BoatClassMasterdata(String displayName, double hullLengthInMeter, String... alternativeNames) {
        this.displayName = displayName;
        this.hullLengthInMeter = hullLengthInMeter;
        this.alternativeNames = alternativeNames;
    }

    public static BoatClassMasterdata resolveBoatClass(String boatClassName) {
        for(BoatClassMasterdata boatClass: values()) {
            if(boatClass.displayName.toUpperCase().equals(boatClassName.toUpperCase())) {
                return boatClass;
            }
            for(String name: boatClass.alternativeNames) {
                if(name.toUpperCase().equals(boatClassName.toUpperCase())) {
                    return boatClass;
                }
            }
        }
        return null;
    }
    
    public double getHullLengthInMeter() {
        return hullLengthInMeter;
    }

    public String getDisplayName() {
        return displayName;
    }
}
