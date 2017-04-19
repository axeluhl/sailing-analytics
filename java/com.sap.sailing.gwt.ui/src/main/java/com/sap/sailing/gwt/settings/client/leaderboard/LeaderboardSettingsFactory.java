package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * A factory class creating leaderboard settings for different contexts (user role, live or replay mode, etc.
 */
public class LeaderboardSettingsFactory {
    private static LeaderboardSettingsFactory instance;
    
    private static final long DEFAULT_REFRESH_INTERVAL = 1000L;
    
    public static LeaderboardSettingsFactory getInstance() {
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
     *            alternatively to <code>nameOfRaceColumnToShow</code>, this argument may be used in case the race name
     *            is known, but its column name is not. If <code>null</code>, and <code>nameOfRaceColumnToShow</code> is
     *            also <code>null</code>, no change to the selected race columns will happen while updating the
     *            leaderboard settings. It is an error to pass non-<code>null</code> values for both,
     *            <code>nameOfRaceColumnToShow</code> <em>and</em> <code>nameOfRaceToShow</code>, and an
     *            {@link IllegalArgumentException} will be thrown in this case.
     * @param raceColumnSelection
     *            the settings will be constructed such that the new settings will have the same race columns selected
     *            as those selected by this argument
     * @param showRegattaRank
     *            if <code>null</code>, the overall detail settings that are responsible for deciding whether or not to
     *            show the regatta rank are left unchanged; otherwise, a non-<code>null</code> overall details
     *            collection will be put to the settings and the {@link DetailType#REGATTA_RANK} will be added to the
     *            overall details if and only if this parameter is <code>true</code>
     * @param raceColumnSelectionType
     * @param raceColumnSelection
     */
    public LeaderboardSettings createNewDefaultSettingsForPlayMode(PlayModes playMode, String nameOfRaceToSort, String nameOfRaceColumnToShow,
            String nameOfRaceToShow, boolean showRegattaRank, boolean showCompetitorSailIdColumn,
            boolean showCompetitorNameColumn, Integer raceColumnSelectionNumber,
            RaceColumnSelectionStrategies raceColumnSelectionType) {
        if (nameOfRaceColumnToShow != null && nameOfRaceToShow != null) {
            throw new IllegalArgumentException("Can identify only one race to show, either by race name or by its column name, but not both");
        }
        LeaderboardSettings settings = null;
        List<String> namesOfRaceColumnsToShow = nameOfRaceColumnToShow == null ? null : Collections.singletonList(nameOfRaceColumnToShow);
        List<String> namesOfRacesToShow = nameOfRaceToShow == null ? null : Collections.singletonList(nameOfRaceToShow);
        final List<DetailType> overallDetails = new ArrayList<>();
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
                raceDetails.add(DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
                raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
                raceDetails.add(DetailType.DISPLAY_LEGS);
                settings = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                        namesOfRaceColumnsToShow,
                    namesOfRacesToShow, raceColumnSelectionNumber,
                        /* set autoExpandPreSelectedRace to true if we look at a single race */ nameOfRaceColumnToShow != null || nameOfRaceToShow != null,
                        /* refresh interval */ DEFAULT_REFRESH_INTERVAL, /* name of race to sort */ nameOfRaceToSort,
                    /* ascending */ true, /* updateUponPlayStateChange */ true, raceColumnSelectionType,
                        /*showAddedScores*/ false, /*showOverallRacesCompleted*/ false,
                        showCompetitorSailIdColumn, showCompetitorNameColumn);
                break;
            case Replay:
                LeaderboardSettings defaultSettings = new LeaderboardSettings();
                settings = new LeaderboardSettings(defaultSettings.getManeuverDetailsToShow(), defaultSettings.getLegDetailsToShow(), defaultSettings.getRaceDetailsToShow(), overallDetails, namesOfRaceColumnsToShow, namesOfRacesToShow,
                        defaultSettings.getNumberOfLastRacesToShow(), nameOfRaceColumnToShow != null, DEFAULT_REFRESH_INTERVAL, nameOfRaceToSort, defaultSettings.isSortAscending(), defaultSettings.isUpdateUponPlayStateChange(), defaultSettings.getActiveRaceColumnSelectionStrategy(), defaultSettings.isShowAddedScores(), defaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), 
                        showCompetitorSailIdColumn, showCompetitorNameColumn);
            break;
        }
        SettingsDefaultValuesUtils.setDefaults(settings, settings);
        return settings;
    }
    
    public LeaderboardSettings createNewDefaultSettingsWithRaceColumns(List<String> namesOfRaceColumns) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings(namesOfRaceColumns);
        SettingsDefaultValuesUtils.setDefaults(leaderboardSettings, leaderboardSettings);
        return leaderboardSettings;
    }
    
    public LeaderboardSettings createNewSettingsWithCustomRaceDetails(List<DetailType> raceDetailsToShow) {
        LeaderboardSettings defaultSettings = new LeaderboardSettings();
        return new LeaderboardSettings(
                Util.cloneListOrNull(defaultSettings.getManeuverDetailsToShow()),
                Util.cloneListOrNull(defaultSettings.getLegDetailsToShow()),
                raceDetailsToShow, Util.cloneListOrNull(defaultSettings.getOverallDetailsToShow()),
                Util.cloneListOrNull(defaultSettings.getNamesOfRaceColumnsToShow()),
                Util.cloneListOrNull(defaultSettings.getNamesOfRacesToShow()),
                defaultSettings.getNumberOfLastRacesToShow(), /* auto-expand pre-selected race */ true,
                defaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultSettings.getNameOfRaceToSort(), defaultSettings.isSortAscending(),
                defaultSettings.isUpdateUponPlayStateChange(),
                defaultSettings.getActiveRaceColumnSelectionStrategy(),
                defaultSettings.isShowAddedScores(),
                defaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                defaultSettings.isShowCompetitorSailIdColumn(),
                defaultSettings.isShowCompetitorFullNameColumn());
    }
    
    public LeaderboardSettings mergeLeaderboardSettings(LeaderboardSettings settingsWithRaceSelection, LeaderboardSettings settingsWithDetails) {
        LeaderboardSettings newSettings = mergeLeaderboardSettingsHelper(settingsWithRaceSelection, settingsWithDetails);
        LeaderboardSettings newDefaultSettings = mergeLeaderboardSettingsHelper(SettingsDefaultValuesUtils.getDefaultSettings(new LeaderboardSettings(), settingsWithRaceSelection), SettingsDefaultValuesUtils.getDefaultSettings(new LeaderboardSettings(), settingsWithDetails));
        SettingsDefaultValuesUtils.keepDefaults(newDefaultSettings, newSettings);
        return newSettings;
    }
    
    private LeaderboardSettings mergeLeaderboardSettingsHelper(LeaderboardSettings settingsWithRaceSelection, LeaderboardSettings settingsWithDetails) {
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
        boolean showCompetitorSailIdColumn = settingsWithRaceSelection.isShowCompetitorSailIdColumn();
        boolean showCompetitorFullNameColumns = settingsWithRaceSelection.isShowCompetitorFullNameColumn();
        String nameOfRaceToSort = settingsWithRaceSelection.getNameOfRaceToSort();
        boolean sortAscending = settingsWithRaceSelection.isSortAscending();
        boolean updateUponPlayStateChange = settingsWithRaceSelection.isUpdateUponPlayStateChange();

        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetailsToShow,
                namesOfRaceColumnsToShow, namesOfRacesToShow, numberOfLastRacesToShow, autoExpandPreSelectedRace, refreshIntervalInMs,
                nameOfRaceToSort, sortAscending, updateUponPlayStateChange, strategy, /*showAddedScores*/ false,
                /*showOverallRacesCompleted*/ false, showCompetitorSailIdColumn, showCompetitorFullNameColumns);
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
