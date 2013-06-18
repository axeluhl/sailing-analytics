package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Comparator;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaRaceStatesComparator implements Comparator<RegattaOverviewEntryDTO> {

    @Override
    public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
        int result = left.courseAreaName.compareTo(right.courseAreaName);
        if (result == 0) {
            if (left.regattaDisplayName != null && right.regattaDisplayName != null) {
                result = new NaturalComparator().compare(left.regattaDisplayName, right.regattaDisplayName);
            }
            if (result == 0) {
                result = left.raceInfo.seriesName.compareTo(right.raceInfo.seriesName);
                if (result == 0) {
                result = Integer.valueOf(left.raceInfo.fleetOrdering).compareTo(Integer.valueOf(right.raceInfo.fleetOrdering));
                if (result == 0) {
                    result = new NaturalComparator().compare(left.raceInfo.raceName, right.raceInfo.raceName);
                }
                }
            }
        }
        return result;
    }

}
