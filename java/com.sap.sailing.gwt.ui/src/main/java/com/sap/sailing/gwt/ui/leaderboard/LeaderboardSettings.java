package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.sap.sailing.domain.common.DetailType;

public class LeaderboardSettings {
    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     */
    private final List<String> namesOfRaceColumnsToShow;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     */
    private final List<String> namesOfRacesToShow;
    private final List<DetailType> maneuverDetailsToShow;
    private final List<DetailType> legDetailsToShow;
    private final List<DetailType> raceDetailsToShow;
    private final boolean autoExpandPreSelectedRace;
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
     * @param namesOfRacesToShow
     * @param autoExpandPreSelectedRace
     * @param delayBetweenAutoAdvancesInMilliseconds
     * @param delayInMilliseconds
     * @param nameOfRaceToSort
     * @param sortAscending
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     */
    public LeaderboardSettings(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, boolean autoExpandPreSelectedRace, Long delayBetweenAutoAdvancesInMilliseconds,
            Long delayInMilliseconds, String nameOfRaceToSort, boolean sortAscending) {
        if (namesOfRacesToShow != null && namesOfRaceColumnsToShow != null) {
            throw new IllegalArgumentException("You can identify races either only by their race or by their column names, not both");
        }
        this.legDetailsToShow = legDetailsToShow;
        this.raceDetailsToShow = raceDetailsToShow;
        this.namesOfRacesToShow = namesOfRacesToShow;
        this.namesOfRaceColumnsToShow = namesOfRaceColumnsToShow;
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
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
    public List<String> getNamesOfRaceColumnsToShow() {
        return namesOfRaceColumnsToShow;
    }

    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public List<String> getNamesOfRacesToShow() {
        return namesOfRacesToShow;
    }

    public boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
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
