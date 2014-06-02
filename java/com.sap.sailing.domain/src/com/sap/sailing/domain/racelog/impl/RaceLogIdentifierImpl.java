package com.sap.sailing.domain.racelog.impl;

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
    public com.sap.sse.common.Util.Triple<String, String, String> getIdentifier() {
        return new com.sap.sse.common.Util.Triple<String, String, String>(template.getParentObjectName(), raceColumnName, fleetName);
    }

    @Override
    public String getDeprecatedIdentifier() {
        return String.format("%s.%s.%s", 
                this.getTemplate().getParentObjectName(),
                this.getRaceColumnName(),
                this.getFleetName());
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
