package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.MeterDistance;


public enum RowingBoatClassMasterdata {
    
    ROWING_BOAT("Rowing Boat", 8.00, 0.80, 2, "RowingBoat");

    private final String displayName;
    private final String[] alternativeNames;
    private final double hullLengthInMeter;
    private final double hullBeamInMeter;
    private final int seats;

    private RowingBoatClassMasterdata(String displayName, double hullLengthInMeter,
            double hullBeamInMeter, int seats, String... alternativeNames) {
        this.displayName = displayName;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.seats = seats;
        this.alternativeNames = alternativeNames;
    }

    private RowingBoatClassMasterdata(String displayName, double hullLengthInMeter,
            double hullBeamInMeter, int seats) {
        this.displayName = displayName;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.seats = seats;
        this.alternativeNames = null;
    }

    public static RowingBoatClassMasterdata resolveBoatClass(String boatClassName) {
        String boatClassNameToResolve = unifyBoatClassName(boatClassName);
        for (RowingBoatClassMasterdata boatClass : values()) {
            if (unifyBoatClassName(boatClass.displayName).equals(boatClassNameToResolve)) {
                return boatClass;
            } else if (boatClass.alternativeNames != null) {
                for (String name : boatClass.alternativeNames) {
                    if (unifyBoatClassName(name).equals(boatClassNameToResolve)) {
                        return boatClass;
                    }
                }
            }
        }
        return null;
    }

    public static String unifyBoatClassName(String boatClassName) {
        return boatClassName == null ? null : boatClassName.toUpperCase().replaceAll("\\s+","");
    }
    
    public Distance getHullLength() {
        return new MeterDistance(hullLengthInMeter);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAlternativeNames() {
        return alternativeNames == null ? new String[0] : alternativeNames;
    }

    public Distance getHullBeam() {
        return new MeterDistance(hullBeamInMeter);
    }

    public int getSeats() {
        return seats;
    }

}
