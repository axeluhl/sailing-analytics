package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

/**
 * A factory class creating leaderboard settings for different contexts (user role, live or replay mode, etc.
 */
public class LeaderboardSettingsFactory {
    private static LeaderboardSettingsFactory instance;
    
    public static LeaderboardSettingsFactory getInstance() {
        if (instance == null) {
            instance = new LeaderboardSettingsFactory();
        }
        return instance;
    }
    
    public LeaderboardSettings createNewDefaultSettingsWithRaceColumns(List<String> namesOfRaceColumns) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings(namesOfRaceColumns);
        SettingsUtil.copyDefaultsFromValues(leaderboardSettings, leaderboardSettings);
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
                defaultSettings.getNumberOfLastRacesToShow(),
                defaultSettings.isAutoExpandPreSelectedRace(),
                defaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultSettings.getNameOfRaceToSort(), defaultSettings.isSortAscending(),
                defaultSettings.isUpdateUponPlayStateChange(),
                defaultSettings.getActiveRaceColumnSelectionStrategy(),
                defaultSettings.isShowAddedScores(),
                defaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                defaultSettings.isShowCompetitorSailIdColumn(),
                defaultSettings.isShowCompetitorFullNameColumn(),
                defaultSettings.isShowRaceRankColumn(),
                defaultSettings.isShowCompetitorNationality());
    }
    
    public LeaderboardSettings createSettingsWithCustomExpandPreselectedRaceState(LeaderboardSettings settings, boolean expandPreselectedRace) {
        return new LeaderboardSettings(
                Util.cloneListOrNull(settings.getManeuverDetailsToShow()),
                Util.cloneListOrNull(settings.getLegDetailsToShow()),
                settings.getRaceDetailsToShow(), Util.cloneListOrNull(settings.getOverallDetailsToShow()),
                Util.cloneListOrNull(settings.getNamesOfRaceColumnsToShow()),
                Util.cloneListOrNull(settings.getNamesOfRacesToShow()),
                settings.getNumberOfLastRacesToShow(), expandPreselectedRace,
                settings.getDelayBetweenAutoAdvancesInMilliseconds(),
                settings.getNameOfRaceToSort(), settings.isSortAscending(),
                settings.isUpdateUponPlayStateChange(),
                settings.getActiveRaceColumnSelectionStrategy(),
                settings.isShowAddedScores(),
                settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(),
                settings.isShowCompetitorFullNameColumn(),
                settings.isShowRaceRankColumn(),
                settings.isShowCompetitorNationality());
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
                /* showOverallRacesCompleted */ false, showCompetitorSailIdColumn, showCompetitorFullNameColumns,
                settingsWithRaceSelection.isShowRaceRankColumn(),
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
