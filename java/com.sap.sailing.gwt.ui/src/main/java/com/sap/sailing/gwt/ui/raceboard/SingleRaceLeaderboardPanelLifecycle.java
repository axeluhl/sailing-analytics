package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

public class SingleRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {
    
    private static final long DEFAULT_REFRESH_INTERVAL = 1000L;
    
    private final boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard;
    private final RegattaAndRaceIdentifier raceIdentifier;

    public SingleRaceLeaderboardPanelLifecycle(RegattaAndRaceIdentifier raceIdentifier, StringMessages stringMessages) {
        super(null, stringMessages);
        this.raceIdentifier = raceIdentifier;
        this.isScreenLargeEnoughToInitiallyDisplayLeaderboard = Document.get().getClientWidth() >= 1024;
    }
    
    @Override
    public LeaderboardSettings createDefaultSettings() {
        List<String> namesOfRaceColumnsToShow = null;
        List<String> namesOfRacesToShow = Collections.singletonList(raceIdentifier.getRaceName());
        
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.RACE_DISTANCE_TRAVELED);
        raceDetails.add(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        raceDetails.add(DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
        raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
        raceDetails.add(DetailType.DISPLAY_LEGS);
        List<DetailType> overallDetails = new ArrayList<>();
        LeaderboardSettings defaultSettings = new LeaderboardSettings();
        LeaderboardSettings  settings = new LeaderboardSettings(defaultSettings.getManeuverDetailsToShow(), defaultSettings.getLegDetailsToShow(), defaultSettings.getRaceDetailsToShow(), overallDetails, namesOfRaceColumnsToShow, namesOfRacesToShow,
                defaultSettings.getNumberOfLastRacesToShow(), false, DEFAULT_REFRESH_INTERVAL, raceIdentifier.getRaceName(), defaultSettings.isSortAscending(), defaultSettings.isUpdateUponPlayStateChange(), defaultSettings.getActiveRaceColumnSelectionStrategy(), defaultSettings.isShowAddedScores(), defaultSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), 
                /*showCompetitorSailIdColumn*/ true,
                /*
                 * don't showCompetitorFullNameColumn in case screen is so small that we don't even display the
                 * leaderboard initially
                 */ isScreenLargeEnoughToInitiallyDisplayLeaderboard, false, false);
        SettingsUtil.copyDefaultsFromValues(settings, settings);
        
        return settings;
    }
    
    public boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard() {
        return isScreenLargeEnoughToInitiallyDisplayLeaderboard;
    }
    
    @Override
    public LeaderboardSettings extractDocumentSettings(LeaderboardSettings currentLeaderboardSettings) {
        LeaderboardSettings defaultLeaderboardSettings = SettingsDefaultValuesUtils.getDefaultSettings(new LeaderboardSettings(), currentLeaderboardSettings);
        LeaderboardSettings contextSpecificLeaderboardSettings = new LeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                defaultLeaderboardSettings.getNamesOfRaceColumnsToShow(),
                defaultLeaderboardSettings.getNamesOfRacesToShow(),
                currentLeaderboardSettings.getNumberOfLastRacesToShow(),
                currentLeaderboardSettings.isAutoExpandPreSelectedRace(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultLeaderboardSettings.getNameOfRaceToSort(), currentLeaderboardSettings.isSortAscending(),
                currentLeaderboardSettings.isUpdateUponPlayStateChange(),
                currentLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowRaceRankColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality());
        SettingsDefaultValuesUtils.keepDefaults(currentLeaderboardSettings, contextSpecificLeaderboardSettings);
        return contextSpecificLeaderboardSettings;
    }
    
    

}
