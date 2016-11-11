package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class RaceBoardComponentContext
        extends AbstractComponentContextWithSettingsStorage<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> {

    public RaceBoardComponentContext(UserService userService, String entryPointId, RaceBoardPerspectiveLifecycle raceBoardPerspectiveLifecycle,
            String regattaName, String raceName, String leaderboardName, String leaderboardGroupName, UUID eventId) {
        super(raceBoardPerspectiveLifecycle, new UserSettingsStorageManager<RaceBoardPerspectiveSettings>(userService, entryPointId + "." + raceBoardPerspectiveLifecycle.getComponentId(), regattaName, raceName, leaderboardName, leaderboardGroupName,
                eventId == null ? null : eventId.toString()));
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        String leaderboardComponentId = rootPerspectiveLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings leaderboardSettings = (LeaderboardSettings) newRootPerspectiveSettings
                .getSettingsPerComponentId().get(leaderboardComponentId);
        PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> defaultSettings = rootPerspectiveLifecycle
                .createDefaultSettings();
        LeaderboardSettings defaultLeaderboardSettings = (LeaderboardSettings) defaultSettings
                .getSettingsPerComponentId().get(leaderboardComponentId);
        LeaderboardSettings contextSpecificLeaderboardSettings = new LeaderboardSettings(
                defaultLeaderboardSettings.getManeuverDetailsToShow(), defaultLeaderboardSettings.getLegDetailsToShow(),
                defaultLeaderboardSettings.getRaceDetailsToShow(), defaultLeaderboardSettings.getOverallDetailsToShow(),
                leaderboardSettings.getNamesOfRaceColumnsToShow(), leaderboardSettings.getNamesOfRacesToShow(),
                defaultLeaderboardSettings.getNumberOfLastRacesToShow(),
                defaultLeaderboardSettings.isAutoExpandPreSelectedRace(),
                defaultLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                leaderboardSettings.getNameOfRaceToSort(), leaderboardSettings.isSortAscending(),
                defaultLeaderboardSettings.isUpdateUponPlayStateChange(),
                defaultLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                defaultLeaderboardSettings.isShowAddedScores(),
                defaultLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                defaultLeaderboardSettings.isShowCompetitorSailIdColumn(),
                defaultLeaderboardSettings.isShowCompetitorFullNameColumn());
        defaultSettings.getSettingsPerComponentId().put(leaderboardComponentId, contextSpecificLeaderboardSettings);
        return defaultSettings;
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        String leaderboardComponentId = rootPerspectiveLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> clonedSettings = rootPerspectiveLifecycle
                .cloneSettings(newRootPerspectiveSettings);
        LeaderboardSettings clonedLeaderboardSettings = (LeaderboardSettings) clonedSettings.getSettingsPerComponentId()
                .get(leaderboardComponentId);
        LeaderboardSettings defaultLeaderboardSettings = rootPerspectiveLifecycle.getLeaderboardPanelLifecycle()
                .createDefaultSettings();
        LeaderboardSettings globalLeaderboardSettings = new LeaderboardSettings(
                clonedLeaderboardSettings.getManeuverDetailsToShow(), clonedLeaderboardSettings.getLegDetailsToShow(),
                clonedLeaderboardSettings.getRaceDetailsToShow(), clonedLeaderboardSettings.getOverallDetailsToShow(),
                defaultLeaderboardSettings.getNamesOfRaceColumnsToShow(),
                defaultLeaderboardSettings.getNamesOfRacesToShow(),
                clonedLeaderboardSettings.getNumberOfLastRacesToShow(),
                clonedLeaderboardSettings.isAutoExpandPreSelectedRace(),
                clonedLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultLeaderboardSettings.getNameOfRaceToSort(), defaultLeaderboardSettings.isSortAscending(),
                clonedLeaderboardSettings.isUpdateUponPlayStateChange(),
                clonedLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                clonedLeaderboardSettings.isShowAddedScores(),
                clonedLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                clonedLeaderboardSettings.isShowCompetitorSailIdColumn(),
                clonedLeaderboardSettings.isShowCompetitorFullNameColumn());
        clonedSettings.getSettingsPerComponentId().put(leaderboardComponentId, globalLeaderboardSettings);
        return clonedSettings;
    }

}
