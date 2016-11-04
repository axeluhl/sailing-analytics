package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class RaceBoardContext extends ComponentContext<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> {
    
    public RaceBoardContext(String entryPointId, RaceBoardPerspectiveLifecycle raceBoardPerspectiveLifecycle, String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId) {
        super(entryPointId, raceBoardPerspectiveLifecycle, regattaName, raceName, leaderboardName, leaderboardGroupName, eventId == null ? null : eventId.toString());
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        String leaderboardComponentId = rootPerspectiveLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings leaderboardSettings = (LeaderboardSettings) newRootPerspectiveSettings.getSettingsPerComponentId().get(leaderboardComponentId);
        PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> defaultSettings = rootPerspectiveLifecycle.createDefaultSettings();
        LeaderboardSettings defaultLeaderboardSettings = (LeaderboardSettings) defaultSettings.getSettingsPerComponentId().get(leaderboardComponentId);
        LeaderboardSettings contextSpecificLeaderboardSettings = new LeaderboardSettings(defaultLeaderboardSettings.getManeuverDetailsToShow(), defaultLeaderboardSettings.getLegDetailsToShow(), defaultLeaderboardSettings.getRaceDetailsToShow(), defaultLeaderboardSettings.getOverallDetailsToShow(), leaderboardSettings.getNamesOfRaceColumnsToShow(), leaderboardSettings.getNamesOfRacesToShow(), leaderboardSettings.getNumberOfLastRacesToShow(), defaultLeaderboardSettings.isAutoExpandPreSelectedRace(), defaultLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(), leaderboardSettings.getNameOfRaceToSort(), leaderboardSettings.isSortAscending(), defaultLeaderboardSettings.isUpdateUponPlayStateChange(), defaultLeaderboardSettings.getActiveRaceColumnSelectionStrategy(), defaultLeaderboardSettings.isShowAddedScores(), defaultLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), defaultLeaderboardSettings.isShowCompetitorSailIdColumn(), defaultLeaderboardSettings.isShowCompetitorFullNameColumn());
        defaultSettings.getSettingsPerComponentId().put(leaderboardComponentId, contextSpecificLeaderboardSettings);
        return defaultSettings;
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
//        String leaderboardComponentId = rootPerspectiveLifecycle.getLeaderboardPanelLifecycle().getComponentId();
//        LeaderboardSettings leaderboardSettings = (LeaderboardSettings) newRootPerspectiveSettings.getSettingsPerComponentId().get(leaderboardComponentId);
//        PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> defaultSettings = rootPerspectiveLifecycle.createDefaultSettings();
//        LeaderboardSettings defaultLeaderboardSettings = (LeaderboardSettings) defaultSettings.getSettingsPerComponentId().get(leaderboardComponentId);
//        LeaderboardSettings contextSpecificLeaderboardSettings = new LeaderboardSettings(defaultLeaderboardSettings.getManeuverDetailsToShow(), defaultLeaderboardSettings.getLegDetailsToShow(), defaultLeaderboardSettings.getRaceDetailsToShow(), defaultLeaderboardSettings.getOverallDetailsToShow(), leaderboardSettings.getNamesOfRaceColumnsToShow(), leaderboardSettings.getNamesOfRacesToShow(), leaderboardSettings.getNumberOfLastRacesToShow(), defaultLeaderboardSettings.isAutoExpandPreSelectedRace(), defaultLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(), leaderboardSettings.getNameOfRaceToSort(), leaderboardSettings.isSortAscending(), defaultLeaderboardSettings.isUpdateUponPlayStateChange(), defaultLeaderboardSettings.getActiveRaceColumnSelectionStrategy(), defaultLeaderboardSettings.isShowAddedScores(), defaultLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), defaultLeaderboardSettings.isShowCompetitorSailIdColumn(), defaultLeaderboardSettings.isShowCompetitorFullNameColumn());
//        defaultSettings.getSettingsPerComponentId().put(leaderboardComponentId, contextSpecificLeaderboardSettings);
        return newRootPerspectiveSettings;
    }
    
}
