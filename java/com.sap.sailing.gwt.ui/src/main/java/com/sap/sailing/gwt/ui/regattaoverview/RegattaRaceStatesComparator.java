package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Comparator;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaRaceStatesComparator implements Comparator<RegattaOverviewEntryDTO> {

    @Override
    public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
        int result = left.courseAreaName.compareTo(right.courseAreaName);
        if (result == 0) {
            if (left.regattaName != null && right.regattaName != null) {
                result = new NaturalComparator().compare(left.regattaName, right.regattaName);
            }
            if (result == 0) {
              //Caution: Series is missing!!!
                result = Integer.valueOf(left.raceInfo.fleetOrdering).compareTo(Integer.valueOf(right.raceInfo.fleetOrdering));
                if (result == 0) {
                    result = (-1) * new NaturalComparator().compare(right.raceInfo.raceName, left.raceInfo.raceName);
                    //this naturalComparator sorts the other way round R30 < R29 < R28 etc.
                }
            }
        }
        return result;
    }

}
