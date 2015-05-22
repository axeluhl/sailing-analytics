package com.sap.sailing.racecommittee.app.ui.comparators;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;

import java.util.Comparator;

public class RaceListDataTypeComparator implements Comparator<RaceListDataType> {

    @Override
    public int compare(RaceListDataType left, RaceListDataType right) {
        RaceListDataTypeRace leftRace = (RaceListDataTypeRace) left;
        RaceListDataTypeRace rightRace = (RaceListDataTypeRace) right;
        return new ManagedRaceStartTimeComparator().compare(leftRace.getRace(), rightRace.getRace());
    }
}
