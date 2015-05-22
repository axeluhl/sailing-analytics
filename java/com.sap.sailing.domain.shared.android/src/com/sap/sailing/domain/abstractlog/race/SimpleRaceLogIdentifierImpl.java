package com.sap.sailing.domain.abstractlog.race;



public class SimpleRaceLogIdentifierImpl implements SimpleRaceLogIdentifier {

    protected final String regattaLikeParentName;
    protected final String raceColumnName;
    protected final String fleetName;
    
    public SimpleRaceLogIdentifierImpl(String regattaLikeParentName, String raceColumnName, String fleetName) {
        this.regattaLikeParentName = regattaLikeParentName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    @Override
    public String getRegattaLikeParentName() {
        return regattaLikeParentName;
    }

    @Override
    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public String getFleetName() {
        return fleetName;
    }

    @Override
    public String toString() {
        return "SimpleRaceLogIdentifier [regattaLikeParentName=" + regattaLikeParentName + ", raceCloumnName=" + raceColumnName
                + ", fleetName=" + fleetName + "]";
    }

    @Override
    public com.sap.sse.common.Util.Triple<String, String, String> getIdentifier() {
        return new com.sap.sse.common.Util.Triple<String, String, String>(
                regattaLikeParentName, raceColumnName, fleetName);
    }

    @Override
    public String getDeprecatedIdentifier() {
        return String.format("%s.%s.%s", 
                regattaLikeParentName,
                raceColumnName,
                fleetName);
    }
}
