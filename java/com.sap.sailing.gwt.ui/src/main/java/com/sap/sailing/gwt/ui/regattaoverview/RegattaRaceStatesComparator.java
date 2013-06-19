package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Comparator;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaRaceStatesComparator implements Comparator<RegattaOverviewEntryDTO> {

    private Comparator<String> nameComparator;

    public RegattaRaceStatesComparator() {
        this.nameComparator = new NaturalComparator();
    }

    @Override
    public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
        int result = nameComparator.compare(left.courseAreaName, right.courseAreaName);
        if (result == 0) {
            if (left.regattaDisplayName != null && right.regattaDisplayName != null) {
                result = nameComparator.compare(left.regattaDisplayName, right.regattaDisplayName);
                if (result == 0) {
                    result = Integer.valueOf(left.raceInfo.seriesOrder).compareTo(right.raceInfo.seriesOrder);
                    if (result == 0) {
                        result = Integer.valueOf(left.raceInfo.fleetOrdering).compareTo(Integer.valueOf(right.raceInfo.fleetOrdering));
                        if (result == 0) {
                            result = nameComparator.compare(left.raceInfo.raceName, right.raceInfo.raceName);
                        }
                    }
                }
            }
        }
        return result;
    }

}
