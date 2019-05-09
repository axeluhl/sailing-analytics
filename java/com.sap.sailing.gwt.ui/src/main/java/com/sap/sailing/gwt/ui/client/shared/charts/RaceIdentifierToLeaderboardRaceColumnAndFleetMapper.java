package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;

/**
 * Given a {@link LeaderboardDTO}, maps a {@link RegattaAndRaceIdentifier} to the triple of
 * leaderboard, race column and fleet which is useful to identify a race log and/or regatta
 * log.<p>
 * 
 * The {@link LeaderboardDTO} can be {@link #setLeaderboard updated} at any time which may affect the result of resolving
 * the race identifier. A typical case would be changing a race's tracking status or attaching or
 * detaching the tracked race from the race column.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class RaceIdentifierToLeaderboardRaceColumnAndFleetMapper {
    private LeaderboardDTO leaderboard;
    
    public static class LeaderboardNameRaceColumnNameAndFleetName {
        private final String leaderboardName;
        private final String raceColumnName;
        private final String fleetName;
        public LeaderboardNameRaceColumnNameAndFleetName(String leaderboardName, String raceColumnName, String fleetName) {
            super();
            this.leaderboardName = leaderboardName;
            this.raceColumnName = raceColumnName;
            this.fleetName = fleetName;
        }
        public String getLeaderboardName() {
            return leaderboardName;
        }
        public String getRaceColumnName() {
            return raceColumnName;
        }
        public String getFleetName() {
            return fleetName;
        }
    }
    public void setLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
    }

    public LeaderboardDTO getLeaderboard() {
        return leaderboard;
    }
    
    public RaceColumnDTO getColumn(RegattaAndRaceIdentifier raceIdentifier) {
        RaceColumnDTO result = null;
        if (leaderboard != null) {
            for (RaceColumnDTO columnDTO : leaderboard.getRaceList()) {
                if (columnDTO.containsRace(raceIdentifier)) {
                    result = columnDTO;
                    break;
                }
            }
        }
        return result;
    }

    public LeaderboardNameRaceColumnNameAndFleetName getLeaderboardNameAndRaceColumnNameAndFleetName(RegattaAndRaceIdentifier raceIdentifier) {
        final LeaderboardNameRaceColumnNameAndFleetName result;
        final RaceColumnDTO raceColumn = getColumn(raceIdentifier);
        if (raceColumn == null) {
            result = null;
        } else {
            final FleetDTO fleet = raceColumn.getFleet(raceIdentifier);
            if (fleet == null) {
                result = null;
            } else {
                result = new LeaderboardNameRaceColumnNameAndFleetName(leaderboard.getName(), raceColumn.getName(), fleet.getName());
            }
        }
        return result;
    }
}
