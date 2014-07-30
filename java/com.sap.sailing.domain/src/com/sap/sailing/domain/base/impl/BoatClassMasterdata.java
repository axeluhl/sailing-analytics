package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.BoatHullType;

public enum BoatClassMasterdata {
    //LASER_SB3 ( "Laser SB3", 6.15 ),
    EXTREME_40 ("Extreme 40", false, 12.2, 6.60, BoatHullType.CATAMARAN),
    J80 ("J/80", true, 8.0, 2.51, BoatHullType.MONOHULL, "J80", "J-80"),
    _49ER ("49er", true, 4.88, 1.93, BoatHullType.MONOHULL);
    //_29ER ("29er", 4.45),
    //_505 ("505", 5.03),
    //_420 ("420", 4.2),
    //_470 ("470", 4.7);


    private final String displayName;
    private final String[] alternativeNames;
    private final double hullLengthInMeter;
    private final double hullBeamInMeter;
    private final BoatHullType hullType;
    private final boolean typicallyStartsUpwind;

    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter, double hullBeamInMeter,
            BoatHullType hullType, String... alternativeNames) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.alternativeNames = alternativeNames;
    }
    
    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter, double hullBeamInMeter,
            BoatHullType hullType) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.alternativeNames = null;
    }    
    
    public BoatClassMasterdata resolveBoatClass(String boatClassName) {
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

    public String[] getAlternativeNames() {
        return alternativeNames;
    }

    public double getHullBeamInMeter() {
        return hullBeamInMeter;
    }

    public BoatHullType getHullType() {
        return hullType;
    }

    public boolean isTypicallyStartsUpwind() {
        return typicallyStartsUpwind;
    }
}
