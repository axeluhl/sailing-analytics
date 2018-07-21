package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;

public class RaceLogIdentifierImpl extends SimpleRaceLogIdentifierImpl implements RaceLogIdentifier {
    private static final long serialVersionUID = -1933109112840405951L;
    
    private final RegattaLikeIdentifier regattaLikeParent;
    
    public RaceLogIdentifierImpl(RegattaLikeIdentifier regattaLikeParent, String raceColumnName, Fleet fleet) {
        this(regattaLikeParent, raceColumnName, fleet.getName());
    }
    
    public RaceLogIdentifierImpl(RegattaLikeIdentifier regattaLikeParent, String raceColumnName, String fleetName) {
        super(regattaLikeParent.getName(), raceColumnName, fleetName);
        this.regattaLikeParent = regattaLikeParent;
    }

    @Override
    public RegattaLikeIdentifier getRegattaLikeParent() {
        return regattaLikeParent;
    }
}
