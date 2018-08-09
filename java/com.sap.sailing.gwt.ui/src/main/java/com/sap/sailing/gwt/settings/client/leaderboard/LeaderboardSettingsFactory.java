package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

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
    

    public MultiRaceLeaderboardSettings createNewDefaultSettingsWithLastN(int numberOfLastRacesToShow) {
        MultiRaceLeaderboardSettings defaultSettings = new MultiRaceLeaderboardSettings();
        return new MultiRaceLeaderboardSettings(
                defaultSettings.getManeuverDetailsToShow(),
                defaultSettings.getLegDetailsToShow(),
                defaultSettings.getRaceDetailsToShow(), defaultSettings.getOverallDetailsToShow(),
                defaultSettings.getNamesOfRaceColumnsToShow(),
                numberOfLastRacesToShow,
                defaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                RaceColumnSelectionStrategies.LAST_N,
                defaultSettings.isShowAddedScores(),
                defaultSettings.isShowCompetitorShortNameColumn(),
                defaultSettings.isShowCompetitorFullNameColumn(),
                defaultSettings.isShowCompetitorBoatInfoColumn(),
                defaultSettings.isShowCompetitorNationality());
    }
 
    public SingleRaceLeaderboardSettings createNewSettingsWithCustomRaceDetails(List<DetailType> raceDetailsToShow) {
        SingleRaceLeaderboardSettings defaultSettings = new SingleRaceLeaderboardSettings(true);
        return new SingleRaceLeaderboardSettings(
                defaultSettings.getManeuverDetailsToShow(),
                defaultSettings.getLegDetailsToShow(),
                raceDetailsToShow, defaultSettings.getOverallDetailsToShow(),
                defaultSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultSettings.isShowAddedScores(),
                defaultSettings.isShowCompetitorShortNameColumn(),
                defaultSettings.isShowCompetitorFullNameColumn(),
                defaultSettings.isShowCompetitorBoatInfoColumn(),
                defaultSettings.isShowRaceRankColumn(),
                defaultSettings.isShowCompetitorNationality());
    }
    
    public MultiRaceLeaderboardSettings mergeLeaderboardSettings(MultiRaceLeaderboardSettings settingsWithRaceSelection, MultiRaceLeaderboardSettings settingsWithDetails) {
        final MultiRaceLeaderboardSettings newSettings = mergeLeaderboardSettingsHelper(settingsWithRaceSelection, settingsWithDetails);
        return newSettings;
    }
    
    private MultiRaceLeaderboardSettings mergeLeaderboardSettingsHelper(MultiRaceLeaderboardSettings settingsWithRaceSelection, MultiRaceLeaderboardSettings settingsWithDetails) {
        Collection<DetailType> maneuverDetails = settingsWithDetails.getManeuverDetailsToShow();
        Collection<DetailType> legDetails = settingsWithDetails.getLegDetailsToShow();
        Collection<DetailType> raceDetails = settingsWithDetails.getRaceDetailsToShow();
        Collection<DetailType> overallDetailsToShow = settingsWithDetails.getOverallDetailsToShow();
        Long refreshIntervalInMs = settingsWithDetails.getDelayBetweenAutoAdvancesInMilliseconds();
        
        RaceColumnSelectionStrategies strategy = settingsWithRaceSelection.getActiveRaceColumnSelectionStrategy();
        List<String> namesOfRaceColumnsToShow = settingsWithRaceSelection.getNamesOfRaceColumnsToShow();
        Integer numberOfLastRacesToShow = settingsWithRaceSelection.getNumberOfLastRacesToShow();
        boolean showCompetitorShortNameColumn = settingsWithRaceSelection.isShowCompetitorShortNameColumn();
        boolean showCompetitorFullNameColumn = settingsWithRaceSelection.isShowCompetitorFullNameColumn();
        boolean showCompetitorBoatInfoColumn = settingsWithRaceSelection.isShowCompetitorBoatInfoColumn();

        return new MultiRaceLeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetailsToShow,
                namesOfRaceColumnsToShow, numberOfLastRacesToShow, refreshIntervalInMs,
                strategy, /*showAddedScores*/ false,
                showCompetitorShortNameColumn, showCompetitorFullNameColumn, showCompetitorBoatInfoColumn,
                settingsWithRaceSelection.isShowCompetitorNationality());
    }
    
}
