package com.sap.sailing.gwt.regattaoverview.client;

import java.util.List;

import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

public class RegattaOverviewUtils {

    public static boolean hasAnyRaceGroupASeries(List<RaceGroupDTO> raceGroupDTOs) {
        boolean result = false;
        if (raceGroupDTOs != null) {
            for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                for (RaceGroupSeriesDTO series : raceGroup.getSeries()) {
                    if (!series.getName().equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static boolean hasAnyRaceGroupAFleet(List<RaceGroupDTO> raceGroupDTOs) {
        boolean result = false;
        if (raceGroupDTOs != null) {
            for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                for (RaceGroupSeriesDTO series : raceGroup.getSeries()) {
                    for (FleetDTO fleet : series.getFleets()) {
                        if (!fleet.getName().equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
}
