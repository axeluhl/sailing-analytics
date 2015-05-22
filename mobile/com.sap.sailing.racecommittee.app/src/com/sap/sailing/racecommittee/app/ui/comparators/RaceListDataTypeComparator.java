package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;

public class RaceListDataTypeComparator implements Comparator<RaceListDataType> {

    @Override
    public int compare(RaceListDataType left, RaceListDataType right) {
        RaceListDataTypeRace leftRace = (RaceListDataTypeRace) left;
        RaceListDataTypeRace rightRace = (RaceListDataTypeRace) right;
        return new ManagedRaceStartTimeComparator().compare(leftRace.getRace(), rightRace.getRace());
    }
}
