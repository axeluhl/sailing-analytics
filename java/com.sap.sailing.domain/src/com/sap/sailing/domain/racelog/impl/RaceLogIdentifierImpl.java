package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;

public class RaceLogIdentifierImpl implements RaceLogIdentifier {
    private static final long serialVersionUID = -1933109112840405951L;
    
    private final String raceColumnName;
    private final String fleetName;
    
    private final RegattaLikeIdentifier regattaLikeParent;
    
    public RaceLogIdentifierImpl(RegattaLikeIdentifier regattaLikeParent, String raceColumnName, Fleet fleet) {
        this.regattaLikeParent = regattaLikeParent;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleet.getName();
    }

    @Override
    public com.sap.sse.common.Util.Triple<String, String, String> getIdentifier() {
        return new com.sap.sse.common.Util.Triple<String, String, String>(
                regattaLikeParent.getName(), raceColumnName, fleetName);
    }

    @Override
    public String getDeprecatedIdentifier() {
        return String.format("%s.%s.%s", 
                regattaLikeParent.getName(),
                raceColumnName,
                fleetName);
    }

    @Override
    public RegattaLikeIdentifier getRegattaLikeParent() {
        return regattaLikeParent;
    }

    @Override
    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public String getFleetName() {
        return fleetName;
    }

}
