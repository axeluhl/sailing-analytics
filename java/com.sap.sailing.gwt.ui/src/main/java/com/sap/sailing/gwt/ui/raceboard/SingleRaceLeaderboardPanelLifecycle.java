package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
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
    private final boolean canBoatsOfCompetitorsChangePerRace;

    public SingleRaceLeaderboardPanelLifecycle(final StringMessages stringMessages,
            final Iterable<DetailType> availableDetailTypes, final boolean canBoatsOfCompetitorsChangePerRace) {
        super(stringMessages, availableDetailTypes);
        this.isScreenLargeEnoughToInitiallyDisplayLeaderboard = Document.get().getClientWidth() >= 1024;
        this.canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace;
    }
    
    @Override
    public SingleRaceLeaderboardSettings createDefaultSettings() {
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.RACE_DISTANCE_TRAVELED);
        raceDetails.add(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        raceDetails.add(DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
        raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
        raceDetails.add(DetailType.RACE_DISPLAY_LEGS);
        List<DetailType> overallDetails = new ArrayList<>();
        SingleRaceLeaderboardSettings defaultSettings = new SingleRaceLeaderboardSettings(canBoatsOfCompetitorsChangePerRace);

        // don't show competitor fullName column if even leaderboard isn't shown initially
        final boolean showCompetitorFullNameColumn = isScreenLargeEnoughToInitiallyDisplayLeaderboard;
        SingleRaceLeaderboardSettings settings = new SingleRaceLeaderboardSettings(
                defaultSettings.getManeuverDetailsToShow(), defaultSettings.getLegDetailsToShow(),
                defaultSettings.getRaceDetailsToShow(), overallDetails, DEFAULT_REFRESH_INTERVAL,
                defaultSettings.isShowAddedScores(),
                /* showCompetitorShortNameColumn */ true,
                showCompetitorFullNameColumn, 
                /* showCompetitorBoatInfoColumn */ canBoatsOfCompetitorsChangePerRace,
                /* isCompetitorNationalityColumnVisible */ false, 
                /* showRaceRankColumn */ false);
        SettingsUtil.copyDefaultsFromValues(settings, settings);
        
        return settings;
    }
    
    @Override
    public SingleRaceLeaderboardSettingsDialogComponent getSettingsDialogComponent(SingleRaceLeaderboardSettings settings) {
        return new SingleRaceLeaderboardSettingsDialogComponent(settings, stringMessages, availableDetailTypes);
    }

}
