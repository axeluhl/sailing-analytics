package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

public class LeaderboardSettings {
    private final List<RaceInLeaderboardDTO> raceColumnsToShow;
    private final List<DetailType> maneuverDetailsToShow;
    private final List<DetailType> legDetailsToShow;
    private final List<DetailType> raceDetailsToShow;
    private final boolean autoExpandFirstRace;
    private final long delayBetweenAutoAdvancesInMilliseconds;
    private final long delayInMilliseconds;
    
    /**
     * An optional sort column; if <code>null</code>, the leaderboard sorting won't be touched when updating the settings.
     * Otherwise, the leaderboard will be sorted by this column (ascending if {@link #sortAscending}, descending otherwise.
     */
    private final SortableColumn<LeaderboardRowDTO, ?> sortByColumn;
    private final boolean sortAscending;
    
    public LeaderboardSettings(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<RaceInLeaderboardDTO> raceColumnsToShow,
            boolean autoExpandFirstRace, long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds,
            SortableColumn<LeaderboardRowDTO, ?> sortByColumn, boolean sortAscending) {
        this.legDetailsToShow = legDetailsToShow;
        this.raceDetailsToShow = raceDetailsToShow;
        this.raceColumnsToShow = raceColumnsToShow;
        this.autoExpandFirstRace = autoExpandFirstRace;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.delayInMilliseconds = delayInMilliseconds;
        this.maneuverDetailsToShow = meneuverDetailsToShow;
        this.sortByColumn = sortByColumn;
        this.sortAscending = sortAscending;
    }
  
    public List<DetailType> getManeuverDetailsToShow() {
        return maneuverDetailsToShow;
    }

    public List<DetailType> getLegDetailsToShow() {
        return legDetailsToShow;
    }

    public List<DetailType> getRaceDetailsToShow() {
        return raceDetailsToShow;
    }
    
    public List<RaceInLeaderboardDTO> getRaceColumnsToShow(){
        return raceColumnsToShow;
    }

    public boolean isAutoExpandFirstRace() {
        return autoExpandFirstRace;
    }

    public long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds;
    }

    public long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }

    public SortableColumn<LeaderboardRowDTO, ?> getSortByColumn() {
        return sortByColumn;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

}
