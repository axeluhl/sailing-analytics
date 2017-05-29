package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
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
    public LeaderboardSettings createNewSettingsForPlayMode(PlayModes playMode, String nameOfRaceToSort, String nameOfRaceColumnToShow,
            String nameOfRaceToShow, boolean showRegattaRank, boolean showCompetitorSailIdColumn,
            boolean showCompetitorNameColumn, Integer raceColumnSelectionNumber,
            RaceColumnSelectionStrategies raceColumnSelectionType) {
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
                raceDetails.add(DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
                raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
                raceDetails.add(DetailType.DISPLAY_LEGS);
                final List<DetailType> overallDetails = createOverallDetailsForShowRegattaRank(showRegattaRank);
                settings = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                        namesOfRaceColumnsToShow,
                    namesOfRacesToShow, raceColumnSelectionNumber,
                        /* set autoExpandPreSelectedRace to true if we look at a single race */ nameOfRaceColumnToShow != null || nameOfRaceToShow != null,
                        /* refresh interval */ null, /* name of race to sort */ nameOfRaceToSort,
                    /* ascending */ true, /* updateUponPlayStateChange */ true, raceColumnSelectionType,
                        /*showAddedScores*/ false, /*showOverallRacesCompleted*/ false,
                    showCompetitorSailIdColumn, showCompetitorNameColumn, false);
                break;
            case Replay:
            settings = createNewDefaultSettings(namesOfRaceColumnsToShow, namesOfRacesToShow, nameOfRaceToSort, /* autoExpandFirstRace */
                    nameOfRaceColumnToShow != null, showRegattaRank, showCompetitorSailIdColumn, showCompetitorNameColumn);
            break;
        }
        return settings;
    }
    
    /**
     * @param showRegattaRank
     *            Puts overall details
     *            collection to the settings and adds {@link DetailType#REGATTA_RANK} to the
     *            overall details if and only if this parameter is <code>true</code>
     */
    private List<DetailType> createOverallDetailsForShowRegattaRank(boolean showRegattaRank) {
        final List<DetailType> overallDetails = new ArrayList<>();
        if (showRegattaRank) {
            overallDetails.add(DetailType.REGATTA_RANK);
        }
        return overallDetails;
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
     * @param showRegattaRank
     *            if <code>null</code>, the overall detail settings that are responsible for deciding whether or not to
     *            show the regatta rank are left unchanged; otherwise, a non-<code>null</code> overall details
     *            collection will be put to the settings and the {@link DetailType#REGATTA_RANK} will be added to the
     *            overall details if and only if this parameter is <code>true</code>
     */
    private LeaderboardSettings createNewDefaultSettings(List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, String nameOfRaceToSort, boolean autoExpandPreSelectedRace, Boolean showRegattaRank,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn) {
        final List<DetailType> overallDetails = createOverallDetailsForShowRegattaRank(showRegattaRank);
        return new LeaderboardSettings(namesOfRaceColumnsToShow, namesOfRacesToShow, overallDetails,
                nameOfRaceToSort, autoExpandPreSelectedRace,
                showCompetitorSailIdColumn, showCompetitorFullNameColumn);
    }
    
    public LeaderboardSettings createNewSettingsWithCustomDefaults(LeaderboardSettings customDefaultSettings, LeaderboardSettings settingsWithCustomValues) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings();
        leaderboardSettings.overrideDefaultValues(customDefaultSettings);
        leaderboardSettings.setValues(settingsWithCustomValues);
        return leaderboardSettings;
    }
    
    public LeaderboardSettings createNewSettingsWithCustomDefaults(LeaderboardSettings customDefaultSettings) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings();
        leaderboardSettings.overrideDefaultValues(customDefaultSettings);
        return leaderboardSettings;
    }
    
    public LeaderboardSettings overrideDefaultValuesWithNewDefaults(LeaderboardSettings settingsWithCustomValues, LeaderboardSettings customDefaultSettings) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings();
        leaderboardSettings.setValues(settingsWithCustomValues);
        leaderboardSettings.overrideDefaultValues(customDefaultSettings);
        return leaderboardSettings;
    }
    
    public LeaderboardSettings keepDefaults(LeaderboardSettings previousSettings, LeaderboardSettings newSettings) {
        LeaderboardSettings leaderboardSettings = previousSettings.getDefaultSettings();
        leaderboardSettings.setValues(newSettings);
        return leaderboardSettings;
    }
    
    public LeaderboardSettings overrideDefaultValuesForRaceDetails(LeaderboardSettings existingSettings, List<DetailType> newRaceDetailsDefaultValues) {
        LeaderboardSettings newSettings = new LeaderboardSettings(
                Util.cloneListOrNull(existingSettings.getManeuverDetailsToShow()),
                Util.cloneListOrNull(existingSettings.getLegDetailsToShow()),
                newRaceDetailsDefaultValues, Util.cloneListOrNull(existingSettings.getOverallDetailsToShow()),
                Util.cloneListOrNull(existingSettings.getNamesOfRaceColumnsToShow()),
                Util.cloneListOrNull(existingSettings.getNamesOfRacesToShow()),
                existingSettings.getNumberOfLastRacesToShow(), /* auto-expand pre-selected race */ true,
                existingSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                existingSettings.getNameOfRaceToSort(), existingSettings.isSortAscending(),
                existingSettings.isUpdateUponPlayStateChange(),
                existingSettings.getActiveRaceColumnSelectionStrategy(),
                existingSettings.isShowAddedScores(),
                existingSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                existingSettings.isShowCompetitorSailIdColumn(),
                existingSettings.isShowCompetitorFullNameColumn(),
                existingSettings.isShowCompetitorNationality());
        
        final LeaderboardSettings existingDefaultSettings = existingSettings.getDefaultSettings();
        
        LeaderboardSettings newDefaultSettings = new LeaderboardSettings(
                Util.cloneListOrNull(existingDefaultSettings.getManeuverDetailsToShow()),
                Util.cloneListOrNull(existingDefaultSettings.getLegDetailsToShow()),
                newRaceDetailsDefaultValues, Util.cloneListOrNull(existingDefaultSettings.getOverallDetailsToShow()),
                Util.cloneListOrNull(existingDefaultSettings.getNamesOfRaceColumnsToShow()),
                Util.cloneListOrNull(existingDefaultSettings.getNamesOfRacesToShow()),
                existingDefaultSettings.getNumberOfLastRacesToShow(), /* auto-expand pre-selected race */ true,
                existingDefaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                existingDefaultSettings.getNameOfRaceToSort(), existingDefaultSettings.isSortAscending(),
                existingDefaultSettings.isUpdateUponPlayStateChange(),
                existingDefaultSettings.getActiveRaceColumnSelectionStrategy(),
                existingDefaultSettings.isShowAddedScores(),
                existingDefaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                existingDefaultSettings.isShowCompetitorSailIdColumn(),
                existingDefaultSettings.isShowCompetitorFullNameColumn(),
                existingSettings.isShowCompetitorNationality());
        
        newDefaultSettings = LeaderboardSettingsFactory.getInstance().createNewSettingsWithCustomDefaults(newDefaultSettings);
        
        newSettings = LeaderboardSettingsFactory.getInstance().keepDefaults(newDefaultSettings, newSettings);
        return newSettings;
    }
    
    public LeaderboardSettings mergeLeaderboardSettings(LeaderboardSettings settingsWithRaceSelection, LeaderboardSettings settingsWithDetails) {
        LeaderboardSettings newSettings = mergeLeaderboardSettingsHelper(settingsWithRaceSelection, settingsWithDetails);
        LeaderboardSettings newDefaultSettings = mergeLeaderboardSettingsHelper(settingsWithRaceSelection.getDefaultSettings(), settingsWithDetails.getDefaultSettings());
        return keepDefaults(newDefaultSettings, newSettings);
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
                /* showOverallRacesCompleted */ false, showCompetitorSailIdColumn, showCompetitorFullNameColumns,
                settingsWithRaceSelection.isShowCompetitorNationality());
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
