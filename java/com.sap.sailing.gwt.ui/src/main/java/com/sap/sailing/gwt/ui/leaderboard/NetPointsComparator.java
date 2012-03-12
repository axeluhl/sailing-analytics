package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class NetPointsComparator implements Comparator<LeaderboardRowDTO> {
    private final boolean ascending;
    private final String raceName;
    
    public NetPointsComparator(boolean ascending, String raceName) {
        super();
        this.ascending = ascending;
        this.raceName = raceName;
    }

    @Override
    public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
        LeaderboardEntryDTO o1Entry = o1.fieldsByRaceName.get(raceName);
        LeaderboardEntryDTO o2Entry = o2.fieldsByRaceName.get(raceName);
        return (o1Entry == null || o1Entry.netPoints == 0) ? (o2Entry == null || o2Entry.netPoints == 0) ? 0
                : ascending ? 1 : -1 : (o2Entry == null || o2Entry.netPoints == 0) ? ascending ? -1 : 1
                : o1Entry.netPoints - o2Entry.netPoints;
    }
}
