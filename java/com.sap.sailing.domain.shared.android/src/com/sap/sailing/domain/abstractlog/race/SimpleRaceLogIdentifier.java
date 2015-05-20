package com.sap.sailing.domain.abstractlog.race;


public class SimpleRaceLogIdentifier{

    private final String regattaLikeParentName;
    private final String raceColumnName;
    private final String fleetName;
    
    public SimpleRaceLogIdentifier(String regattaLikeParentName, String raceColumnName, String fleetName) {
        this.regattaLikeParentName = regattaLikeParentName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    public String getRegattaLikeParentName() {
        return regattaLikeParentName;
    }

    public String getRaceColumnName() {
        return raceColumnName;
    }

    public String getFleetName() {
        return fleetName;
    }

    @Override
    public String toString() {
        return "SimpleRaceLogIdentifier [regattaLikeParentName=" + regattaLikeParentName + ", raceCloumnName=" + raceColumnName
                + ", fleetName=" + fleetName + "]";
    }
}
