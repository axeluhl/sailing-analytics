package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;

public class RaceLogIdentifierImpl implements RaceLogIdentifier {
    private static final long serialVersionUID = -1933109112840405951L;
    
    private final String raceColumnName;
    private final String fleetName;
    
    private final RaceLogIdentifierTemplate template;
    
    public RaceLogIdentifierImpl(RaceLogIdentifierTemplate template, String raceColumnName, Fleet fleet) {
        this.template = template;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleet.getName();
    }

    @Override
    public Serializable getIdentifier() {
        return String.format("%s.%s.%s", template.getParentObjectName(), raceColumnName, fleetName);
    }

    @Override
    public RaceLogIdentifierTemplate getTemplate() {
        return template;
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
