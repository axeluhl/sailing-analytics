package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class NetPointsComparator implements Comparator<LeaderboardRowDTO> {
    private final SortOrderCalculator sortOrderCalculator;
    private final String raceName;
    
    public interface SortOrderCalculator {
        boolean isAscending();
    }
    
    public NetPointsComparator(SortOrderCalculator sortOrderCalculator, String raceName) {
        super();
        this.sortOrderCalculator = sortOrderCalculator;
        this.raceName = raceName;
    }

    /**
     * sorts a meaningless <code>0</code> value to the bottom always, regardless whether ascending/descending sorting is
     * requested
     */
    @Override
    public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
        boolean ascending = sortOrderCalculator.isAscending();
        LeaderboardEntryDTO o1Entry = o1.fieldsByRaceName.get(raceName);
        LeaderboardEntryDTO o2Entry = o2.fieldsByRaceName.get(raceName);
        return (o1Entry == null || o1Entry.netPoints == 0) ? (o2Entry == null || o2Entry.netPoints == 0) ? 0
                : ascending ? 1 : -1 : (o2Entry == null || o2Entry.netPoints == 0) ? ascending ? -1 : 1
                : o1Entry.netPoints - o2Entry.netPoints;
    }
}
