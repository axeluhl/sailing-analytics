package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;

import java.io.Serializable;

public class ManagedRaceIdentifierImpl extends FleetIdentifierImpl implements ManagedRaceIdentifier {

    private String raceName;

    public ManagedRaceIdentifierImpl(String raceName, Fleet fleetWithRaceNames, SeriesBase series, RaceGroup raceGroup) {
        super(fleetWithRaceNames, series, raceGroup);
        this.raceName = raceName;
    }

    public ManagedRaceIdentifierImpl(String raceName, FleetIdentifier identifier) {
        this(raceName, identifier.getFleet(), identifier.getSeries(), identifier.getRaceGroup());
    }

    public String getRaceName() {
        return raceName;
    }

    @Override
    public Serializable getId() {
        return String.format("%s.%s", super.getId(), escapeIdentifierFragment(getRaceName()));
    }

}
