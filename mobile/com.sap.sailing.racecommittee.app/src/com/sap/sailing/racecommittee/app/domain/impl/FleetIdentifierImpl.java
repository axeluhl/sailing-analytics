package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;

import java.io.Serializable;

public class FleetIdentifierImpl implements FleetIdentifier {

    private Fleet fleet;
    private SeriesBase series;
    private RaceGroup raceGroup;

    public FleetIdentifierImpl(Fleet fleet, SeriesBase series, RaceGroup raceGroup) {
        this.fleet = fleet;
        this.series = series;
        this.raceGroup = raceGroup;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public SeriesBase getSeries() {
        return series;
    }

    public RaceGroup getRaceGroup() {
        return raceGroup;
    }

    public Serializable getId() {
        return String.format("%s.%s.%s", 
                escapeIdentifierFragment(getRaceGroup().getName()), 
                escapeIdentifierFragment(getSeries().getName()), 
                escapeIdentifierFragment(getFleet().getName()));
    }
    
    protected String escapeIdentifierFragment(String fragment) {
        return fragment.replace("\\", "\\\\").replace(".", "\\.");
    }

}
