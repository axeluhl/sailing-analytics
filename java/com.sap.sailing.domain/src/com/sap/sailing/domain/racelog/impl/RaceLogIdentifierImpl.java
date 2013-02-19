package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;

public class RaceLogIdentifierImpl implements RaceLogIdentifier {

    private final String raceColumnName;
    private final String fleetName;
    
    private final RaceLogIdentifierTemplate template;
    
    public RaceLogIdentifierImpl(RaceLogIdentifierTemplate template, RaceColumn raceColumn, Fleet fleet) {
        this.template = template;
        this.raceColumnName = raceColumn.getName();
        this.fleetName = fleet.getName();
    }

    @Override
    public Serializable getIdentifier() {
        return String.format("%s.%s.%s", template.getHostName(), raceColumnName, fleetName);
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
