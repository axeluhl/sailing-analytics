package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;

public class ManagedRaceIdentifierImpl extends FleetIdentifierImpl implements ManagedRaceIdentifier {

    private String raceColumnName;

    public ManagedRaceIdentifierImpl(String raceColumnName, Fleet fleetWithRaceNames, SeriesWithRows series,
            RaceGroup raceGroup) {
        super(fleetWithRaceNames, series, raceGroup);
        this.raceColumnName = raceColumnName;
    }

    public ManagedRaceIdentifierImpl(String raceColumnName, FleetIdentifier identifier) {
        this(raceColumnName, identifier.getFleet(), identifier.getSeries(), identifier.getRaceGroup());
    }

    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public String getId() {
        return String.format("%s.%s", super.getId(), escapeIdentifierFragment(getRaceColumnName()));
    }

    @Override
    public String toString() {
        return getRaceColumnName() + " - " + getFleet().getName();
    }
}
