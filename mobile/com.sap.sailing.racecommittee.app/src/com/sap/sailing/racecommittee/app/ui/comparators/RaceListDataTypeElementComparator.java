package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;

public class RaceListDataTypeElementComparator implements Comparator<RaceListDataTypeRace> {

    private Comparator<String> nameComparator;

    public RaceListDataTypeElementComparator() {
        this.nameComparator = new NaturalComparator();
    }

    public int compare(RaceListDataTypeRace lhs, RaceListDataTypeRace rhs) {
        String leftRaceName = lhs.getRaceName();
        String rightRaceName = rhs.getRaceName();

        return nameComparator.compare(leftRaceName, rightRaceName);
    }

}
