package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * A factory class creating leaderboard settings for different contexts (user role, live or replay mode, etc.
 */
public class LeaderboardSettingsFactory {
    private static LeaderboardSettingsFactory instance;
    
    public synchronized static LeaderboardSettingsFactory getInstance() {
        if (instance == null) {
            instance = new LeaderboardSettingsFactory();
        }
        return instance;
    }

    /**
     * @param nameOfRaceToSort
     *            if <code>null</code>, don't sort any race column
     * @param nameOfRaceColumnToShow
     *            if <code>null</code>, the settings returned will cause the list of race columns shown to remain
     *            unchanged during {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}; otherwise, the settings
     *            will show the single race column identified by this argument
     * @param nameOfRaceToShow
     *            alternatively to <code>nameOfRaceColumnToShow</code>, this argument may be used in case the race name is known,
     *            but its column name is not. If <code>null</code>, and <code>nameOfRaceColumnToShow</code> is also <code>null</code>,
     *            no change to the selected race columns will happen while updating the leaderboard settings. It is an error
     *            to pass non-<code>null</code> values for both, <code>nameOfRaceColumnToShow</code> <em>and</em>
     *            <code>nameOfRaceToShow</code>, and an {@link IllegalArgumentException} will be thrown in this case.
     * @param raceColumnSelection 
     *            the settings will be constructed such that the new settings will have the same race columns selected as those selected by
     *            this argument
     */
    public LeaderboardSettings createNewSettingsForPlayMode(PlayModes playMode, String nameOfRaceToSort, String nameOfRaceColumnToShow,
            String nameOfRaceToShow, RaceColumnSelection raceColumnSelection) {
        if (nameOfRaceColumnToShow != null && nameOfRaceToShow != null) {
            throw new IllegalArgumentException("Can identify only one race to show, either by race name or by its column name, but not both");
        }
        LeaderboardSettings settings = null;
        List<String> namesOfRaceColumnsToShow = nameOfRaceColumnToShow == null ? null : Collections.singletonList(nameOfRaceColumnToShow);
        List<String> namesOfRacesToShow = nameOfRaceToShow == null ? null : Collections.singletonList(nameOfRaceToShow);
        switch (playMode) {
            case Live:  
                List<DetailType> maneuverDetails = new ArrayList<DetailType>();
                maneuverDetails.add(DetailType.TACK);
                maneuverDetails.add(DetailType.JIBE);
                maneuverDetails.add(DetailType.PENALTY_CIRCLE);
                List<DetailType> legDetails = new ArrayList<DetailType>();
                legDetails.add(DetailType.DISTANCE_TRAVELED);
                legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
                legDetails.add(DetailType.ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS);
                legDetails.add(DetailType.RANK_GAIN);
                List<DetailType> raceDetails = new ArrayList<DetailType>();
                raceDetails.add(DetailType.RACE_DISTANCE_TRAVELED);
                raceDetails.add(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
                raceDetails.add(DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS);
                raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
                raceDetails.add(DetailType.DISPLAY_LEGS);
                List<DetailType> overallDetails = null; // lead overall details unchanged
                settings = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                        namesOfRaceColumnsToShow,
                        namesOfRacesToShow, raceColumnSelection.getNumberOfLastRaceColumnsToShow(),
                        /* set autoExpandPreSelectedRace to true if we look at a single race */ nameOfRaceColumnToShow != null || nameOfRaceToShow != null,
                        /* refresh interval */ null, /* name of race to sort */ nameOfRaceToSort,
                        /* ascending */ true, /* updateUponPlayStateChange */ true, raceColumnSelection.getType(),
                        /*showAddedScores*/ false, /*showOverallRacesCompleted*/ false);
                break;
            case Replay:
            settings = createNewDefaultSettings(namesOfRaceColumnsToShow, namesOfRacesToShow, nameOfRaceToSort, /* autoExpandFirstRace */
                    nameOfRaceColumnToShow != null);
            break;
        }
        return settings;
    }

    /**
     * @param namesOfRaceColumnsToShow
     *            if <code>null</code>, create settings which leave the list of races to show unchanged when applied
     *            using {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}; otherwise, the list of names of
     *            the race columns (not their races!) that will be shown, so that, e.g., an empty list causes no race
     *            columns to be shown
     * @param namesOfRacesToShow
     *            alternatively, races to show can also be specified by their race names; if not <code>null</code>,
     *            <code>namesOfRaceColumnsToShow</code> must be <code>null</code>
     * @param showOverallLeaderboardsOnSamePage
     *            tells whether a meta leaderboard should be shown beneath the actual leaderboard on the same page, if
     *            such a meta leaderboard exists; if multiple such meta leaderboards exist, they are all shown
     */
    public LeaderboardSettings createNewDefaultSettings(List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, String nameOfRaceToSort, boolean autoExpandPreSelectedRace) {
        return createNewDefaultSettings(namesOfRaceColumnsToShow, namesOfRacesToShow, /* leave overallDetailsToShow unchanged */ null,
                nameOfRaceToSort, autoExpandPreSelectedRace, /* refreshIntervalMillis */ null);
    }
    
    /**
     * Like {@link #createNewDefaultSettings(List, List, String, boolean, boolean)}, only that an additional refresh interval for auto-refresh
     * may be specified; if <code>null</code>, no auto-refresh shall be performed
     */
    public LeaderboardSettings createNewDefaultSettings(List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, List<DetailType> overallDetailsToShow, String nameOfRaceToSort,
            boolean autoExpandPreSelectedRace, Long refreshIntervalMillis) {
        return createNewDefaultSettings(namesOfRaceColumnsToShow, namesOfRacesToShow, overallDetailsToShow,
                nameOfRaceToSort, autoExpandPreSelectedRace, refreshIntervalMillis,
                /* numberOfLastRacesToShow */null, /* raceColumnSelectionStrategy */ RaceColumnSelectionStrategies.EXPLICIT);
    }
    
    public LeaderboardSettings createNewDefaultSettings(List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, List<DetailType> overallDetailsToShow, String nameOfRaceToSort,
            boolean autoExpandPreSelectedRace, Long refreshIntervalMillis, Integer numberOfLastRacesToShow,
            RaceColumnSelectionStrategies raceColumnSelectionStrategy) {
        if (namesOfRaceColumnsToShow != null && namesOfRacesToShow != null) {
            throw new IllegalArgumentException("Can specify race columns either by column or by race name, not both");
        }
        List<DetailType> maneuverDetails = new ArrayList<DetailType>();
        maneuverDetails.add(DetailType.TACK);
        maneuverDetails.add(DetailType.JIBE);
        maneuverDetails.add(DetailType.PENALTY_CIRCLE);
        List<DetailType> legDetails = new ArrayList<DetailType>();
        legDetails.add(DetailType.DISTANCE_TRAVELED);
        legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        legDetails.add(DetailType.RANK_GAIN);
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.DISPLAY_LEGS);
        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetailsToShow,
                namesOfRaceColumnsToShow,
                namesOfRacesToShow, numberOfLastRacesToShow,
                autoExpandPreSelectedRace, refreshIntervalMillis, /* sort by column */ nameOfRaceToSort,
                /* ascending */ true, /* updateUponPlayStateChange */ true, raceColumnSelectionStrategy,
                /*showAddedScores*/ false, /*showOverallRacesCompleted*/ false);
    }
    
    public LeaderboardSettings mergeLeaderboardSettings(LeaderboardSettings settingsWithRaceSelection, LeaderboardSettings settingsWithDetails) {
        List<DetailType> maneuverDetails = copyDetailTypes(settingsWithDetails.getManeuverDetailsToShow());
        List<DetailType> legDetails = copyDetailTypes(settingsWithDetails.getLegDetailsToShow());
        List<DetailType> raceDetails = copyDetailTypes(settingsWithDetails.getRaceDetailsToShow());
        List<DetailType> overallDetailsToShow = copyDetailTypes(settingsWithDetails.getOverallDetailsToShow());
        Long refreshIntervalInMs = settingsWithDetails.getDelayBetweenAutoAdvancesInMilliseconds();
        
        RaceColumnSelectionStrategies strategy = settingsWithRaceSelection.getActiveRaceColumnSelectionStrategy();
        List<String> namesOfRaceColumnsToShow = copyRaceNamesList(settingsWithRaceSelection.getNamesOfRaceColumnsToShow());
        List<String> namesOfRacesToShow = copyRaceNamesList(settingsWithRaceSelection.getNamesOfRacesToShow());
        Integer numberOfLastRacesToShow = settingsWithRaceSelection.getNumberOfLastRacesToShow();
        boolean autoExpandPreSelectedRace = settingsWithRaceSelection.isAutoExpandPreSelectedRace();
        String nameOfRaceToSort = settingsWithRaceSelection.getNameOfRaceToSort();
        boolean sortAscending = settingsWithRaceSelection.isSortAscending();
        boolean updateUponPlayStateChange = settingsWithRaceSelection.isUpdateUponPlayStateChange();

        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetailsToShow,
                namesOfRaceColumnsToShow, namesOfRacesToShow, numberOfLastRacesToShow, autoExpandPreSelectedRace, refreshIntervalInMs,
                nameOfRaceToSort, sortAscending, updateUponPlayStateChange, strategy, /*showAddedScores*/ false,
                /*showOverallRacesCompleted*/ false);
    }
    
    private List<DetailType> copyDetailTypes(List<DetailType> detailTypes) {
        List<DetailType> result = null;
        if(detailTypes != null) {
            result = new ArrayList<DetailType>(detailTypes);
        }
        return result;
    }
    
    private List<String> copyRaceNamesList(List<String> raceNames) {
        List<String> result = null;
        if(raceNames != null) {
            result = new ArrayList<String>(raceNames);
        }
        return result;
    }

}
