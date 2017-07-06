package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
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
    
    public MultiRaceLeaderboardSettings createNewDefaultSettingsWithRaceColumns(List<String> namesOfRaceColumns) {
        MultiRaceLeaderboardSettings leaderboardSettings = new MultiRaceLeaderboardSettings(namesOfRaceColumns);
        SettingsUtil.copyDefaultsFromValues(leaderboardSettings, leaderboardSettings);
        return leaderboardSettings;
    }
 
    public SingleRaceLeaderboardSettings createNewSettingsWithCustomRaceDetails(List<DetailType> raceDetailsToShow) {
        SingleRaceLeaderboardSettings defaultSettings = new SingleRaceLeaderboardSettings();
        return new SingleRaceLeaderboardSettings(
                defaultSettings.getManeuverDetailsToShow(),
                defaultSettings.getLegDetailsToShow(),
                raceDetailsToShow, defaultSettings.getOverallDetailsToShow(),
                defaultSettings.isAutoExpandPreSelectedRace(),
                defaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultSettings.getNameOfRaceToSort(), defaultSettings.isSortAscending(),
                defaultSettings.isUpdateUponPlayStateChange(),
                defaultSettings.getActiveRaceColumnSelectionStrategy(),
                defaultSettings.isShowAddedScores(),
                defaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                defaultSettings.isShowCompetitorSailIdColumn(),
                defaultSettings.isShowCompetitorFullNameColumn(),
                defaultSettings.isShowCompetitorNationality());
    }
    
    public SingleRaceLeaderboardSettings createSettingsWithCustomExpandPreselectedRaceState(SingleRaceLeaderboardSettings settings, boolean expandPreselectedRace) {
        return new SingleRaceLeaderboardSettings(
                settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(),
                settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(),
                expandPreselectedRace,
                settings.getDelayBetweenAutoAdvancesInMilliseconds(),
                settings.getNameOfRaceToSort(), settings.isSortAscending(),
                settings.isUpdateUponPlayStateChange(),
                settings.getActiveRaceColumnSelectionStrategy(),
                settings.isShowAddedScores(),
                settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(),
                settings.isShowCompetitorFullNameColumn(),
                settings.isShowCompetitorNationality());
    }
    
    public MultiRaceLeaderboardSettings mergeLeaderboardSettings(MultiRaceLeaderboardSettings settingsWithRaceSelection, MultiRaceLeaderboardSettings settingsWithDetails) {
        MultiRaceLeaderboardSettings newSettings = mergeLeaderboardSettingsHelper(settingsWithRaceSelection, settingsWithDetails);
        MultiRaceLeaderboardSettings newDefaultSettings = mergeLeaderboardSettingsHelper(SettingsDefaultValuesUtils.getDefaultSettings(new MultiRaceLeaderboardSettings(), settingsWithRaceSelection), SettingsDefaultValuesUtils.getDefaultSettings(new MultiRaceLeaderboardSettings(), settingsWithDetails));
        SettingsDefaultValuesUtils.keepDefaults(newDefaultSettings, newSettings);
        return newSettings;
    }
    
    private MultiRaceLeaderboardSettings mergeLeaderboardSettingsHelper(MultiRaceLeaderboardSettings settingsWithRaceSelection, MultiRaceLeaderboardSettings settingsWithDetails) {
        Collection<DetailType> maneuverDetails = settingsWithDetails.getManeuverDetailsToShow();
        Collection<DetailType> legDetails = settingsWithDetails.getLegDetailsToShow();
        Collection<DetailType> raceDetails = settingsWithDetails.getRaceDetailsToShow();
        Collection<DetailType> overallDetailsToShow = settingsWithDetails.getOverallDetailsToShow();
        Long refreshIntervalInMs = settingsWithDetails.getDelayBetweenAutoAdvancesInMilliseconds();
        
        RaceColumnSelectionStrategies strategy = settingsWithRaceSelection.getActiveRaceColumnSelectionStrategy();
        List<String> namesOfRaceColumnsToShow = copyRaceNamesList(settingsWithRaceSelection.getNamesOfRaceColumnsToShow());
        List<String> namesOfRacesToShow = copyRaceNamesList(settingsWithRaceSelection.getNamesOfRacesToShow());
        Integer numberOfLastRacesToShow = settingsWithRaceSelection.getNumberOfLastRacesToShow();
        boolean showCompetitorSailIdColumn = settingsWithRaceSelection.isShowCompetitorSailIdColumn();
        boolean showCompetitorFullNameColumns = settingsWithRaceSelection.isShowCompetitorFullNameColumn();
        String nameOfRaceToSort = settingsWithRaceSelection.getNameOfRaceToSort();
        boolean sortAscending = settingsWithRaceSelection.isSortAscending();
        boolean updateUponPlayStateChange = settingsWithRaceSelection.isUpdateUponPlayStateChange();

        return new MultiRaceLeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetailsToShow,
                namesOfRaceColumnsToShow, namesOfRacesToShow, numberOfLastRacesToShow, refreshIntervalInMs,
                nameOfRaceToSort, sortAscending, updateUponPlayStateChange, strategy, /*showAddedScores*/ false,
                /* showOverallRacesCompleted */ false, showCompetitorSailIdColumn, showCompetitorFullNameColumns,
                settingsWithRaceSelection.isShowCompetitorNationality());
    }
    
    private List<String> copyRaceNamesList(List<String> raceNames) {
        List<String> result = null;
        if(raceNames != null) {
            result = new ArrayList<String>(raceNames);
        }
        return result;
    }
    
}
