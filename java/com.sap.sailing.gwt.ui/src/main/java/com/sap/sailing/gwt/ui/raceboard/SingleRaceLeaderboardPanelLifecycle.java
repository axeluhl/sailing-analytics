package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.SingleRaceLeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.SingleRaceLeaderboardSettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

public class SingleRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle<SingleRaceLeaderboardSettings> {
    
    private static final long DEFAULT_REFRESH_INTERVAL = 1000L;
    
    private final boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard;

    public SingleRaceLeaderboardPanelLifecycle(StringMessages stringMessages, Collection<DetailType> availableDetailTypes) {
        super(stringMessages, availableDetailTypes);
        this.isScreenLargeEnoughToInitiallyDisplayLeaderboard = Document.get().getClientWidth() >= 1024;
    }
    
    @Override
    public SingleRaceLeaderboardSettings createDefaultSettings() {
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.RACE_DISTANCE_TRAVELED);
        raceDetails.add(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        raceDetails.add(DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
        raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
        raceDetails.add(DetailType.DISPLAY_LEGS);
        List<DetailType> overallDetails = new ArrayList<>();
        SingleRaceLeaderboardSettings defaultSettings = new SingleRaceLeaderboardSettings();
        SingleRaceLeaderboardSettings settings = new SingleRaceLeaderboardSettings(
                defaultSettings.getManeuverDetailsToShow(), defaultSettings.getLegDetailsToShow(),
                defaultSettings.getRaceDetailsToShow(), overallDetails, DEFAULT_REFRESH_INTERVAL,
                defaultSettings.isShowAddedScores(), /* showCompetitorSailIdColumn */ true,
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
    public SingleRaceLeaderboardSettings extractDocumentSettings(SingleRaceLeaderboardSettings currentLeaderboardSettings) {
        SingleRaceLeaderboardSettings defaultLeaderboardSettings = createDefaultSettings();
        SingleRaceLeaderboardSettings contextSpecificLeaderboardSettings = new SingleRaceLeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowRaceRankColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality());
        return SettingsUtil.copyValues(contextSpecificLeaderboardSettings, defaultLeaderboardSettings);
    }
    
    @Override
    public SingleRaceLeaderboardSettings extractUserSettings(SingleRaceLeaderboardSettings currentLeaderboardSettings) {
        SingleRaceLeaderboardSettings defaultLeaderboardSettings = createDefaultSettings();
        SingleRaceLeaderboardSettings globalLeaderboardSettings = new SingleRaceLeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality(),currentLeaderboardSettings.isShowRaceRankColumn());
        return SettingsUtil.copyValues(globalLeaderboardSettings, defaultLeaderboardSettings);
    }

    @Override
    public SingleRaceLeaderboardSettingsDialogComponent getSettingsDialogComponent(SingleRaceLeaderboardSettings settings) {
        return new SingleRaceLeaderboardSettingsDialogComponent(settings, stringMessages, availableDetailTypes);
    }

}
