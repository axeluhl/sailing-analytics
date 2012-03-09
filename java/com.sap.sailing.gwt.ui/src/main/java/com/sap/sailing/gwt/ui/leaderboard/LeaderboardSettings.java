package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

public class LeaderboardSettings {
    private final List<RaceInLeaderboardDTO> raceColumnsToShow;
    private final List<DetailType> maneuverDetailsToShow;
    private final List<DetailType> legDetailsToShow;
    private final List<DetailType> raceDetailsToShow;
    private final boolean autoExpandFirstRace;
    private final Long delayBetweenAutoAdvancesInMilliseconds;
    private final Long delayInMilliseconds;
    
    /**
     * An optional sort column; if <code>null</code>, the leaderboard sorting won't be touched when updating the settings.
     * Otherwise, the leaderboard will be sorted by the race column (ascending if {@link #sortAscending}, descending otherwise.
     */
    private final String nameOfRaceToSort;
    private final boolean sortAscending;
    
    /**
     * @param meneuverDetailsToShow
     * @param legDetailsToShow
     * @param raceDetailsToShow
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     * @param autoExpandFirstRace
     * @param delayBetweenAutoAdvancesInMilliseconds
     * @param delayInMilliseconds
     * @param nameOfRaceToSort
     * @param sortAscending
     */
    public LeaderboardSettings(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<RaceInLeaderboardDTO> raceColumnsToShow,
            boolean autoExpandFirstRace, Long delayBetweenAutoAdvancesInMilliseconds, Long delayInMilliseconds,
            String nameOfRaceToSort, boolean sortAscending) {
        this.legDetailsToShow = legDetailsToShow;
        this.raceDetailsToShow = raceDetailsToShow;
        this.raceColumnsToShow = raceColumnsToShow;
        this.autoExpandFirstRace = autoExpandFirstRace;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.delayInMilliseconds = delayInMilliseconds;
        this.maneuverDetailsToShow = meneuverDetailsToShow;
        this.nameOfRaceToSort = nameOfRaceToSort;
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
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public List<RaceInLeaderboardDTO> getRaceColumnsToShow() {
        return raceColumnsToShow;
    }

    public boolean isAutoExpandFirstRace() {
        return autoExpandFirstRace;
    }

    /**
     * @return if <code>null</code>, leave refresh interval alone (don't change in
     *         {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}
     */
    public Long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds;
    }

    /**
     * @return if <code>null</code>, leave delay alone (don't change in {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}
     */
    public Long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }

    public String getNameOfRaceToSort() {
        return nameOfRaceToSort;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

}
