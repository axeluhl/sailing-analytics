package com.sap.sailing.gwt.ui.raceboard;

import java.util.Map;
import java.util.UUID;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class RaceBoardComponentContext
        extends AbstractComponentContextWithSettingsStorage<RaceBoardPerspectiveLifecycle, PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>> {

    public RaceBoardComponentContext(UserService userService, String entryPointId, RaceBoardPerspectiveLifecycle raceBoardPerspectiveLifecycle,
            String regattaName, String raceName, String leaderboardName, String leaderboardGroupName, UUID eventId) {
        super(raceBoardPerspectiveLifecycle, new UserSettingsStorageManager<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>>(userService, entryPointId + "." + raceBoardPerspectiveLifecycle.getComponentId(), UserSettingsStorageManager.buildContextDefinitionId(regattaName, raceName, leaderboardName, leaderboardGroupName,
                eventId == null ? null : eventId.toString())));
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        String leaderboardComponentId = rootLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings leaderboardSettings = (LeaderboardSettings) newRootPerspectiveSettings
                .getSettingsPerComponentId().get(leaderboardComponentId);
        PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> defaultSettings = rootLifecycle
                .createDefaultSettings();
        LeaderboardSettings defaultLeaderboardSettings = leaderboardSettings.getDefaultSettings();
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
        contextSpecificLeaderboardSettings = LeaderboardSettingsFactory.getInstance().keepDefaults(leaderboardSettings, contextSpecificLeaderboardSettings);
        
        Map<String, Settings> clonedComponentIdsAndSettings = rootLifecycle.cloneComponentIdsAndSettings(defaultSettings);
        clonedComponentIdsAndSettings.put(leaderboardComponentId, contextSpecificLeaderboardSettings);
        return new PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>(defaultSettings.getPerspectiveOwnSettings(), clonedComponentIdsAndSettings);
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        String leaderboardComponentId = rootLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings currentLeaderboardSettings = (LeaderboardSettings) newRootPerspectiveSettings.getSettingsPerComponentId()
                .get(leaderboardComponentId);
        LeaderboardSettings defaultLeaderboardSettings = currentLeaderboardSettings.getDefaultSettings();
        LeaderboardSettings globalLeaderboardSettings = new LeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                defaultLeaderboardSettings.getNamesOfRaceColumnsToShow(),
                defaultLeaderboardSettings.getNamesOfRacesToShow(),
                currentLeaderboardSettings.getNumberOfLastRacesToShow(),
                defaultLeaderboardSettings.isAutoExpandPreSelectedRace(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                defaultLeaderboardSettings.getNameOfRaceToSort(), defaultLeaderboardSettings.isSortAscending(),
                currentLeaderboardSettings.isUpdateUponPlayStateChange(),
                currentLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn());
        globalLeaderboardSettings = LeaderboardSettingsFactory.getInstance().keepDefaults(currentLeaderboardSettings, globalLeaderboardSettings);
        Map<String, Settings> clonedComponentIdsAndSettings = rootLifecycle.cloneComponentIdsAndSettings(newRootPerspectiveSettings);
        clonedComponentIdsAndSettings.put(leaderboardComponentId, globalLeaderboardSettings);
        return new PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>(newRootPerspectiveSettings.getPerspectiveOwnSettings(), clonedComponentIdsAndSettings);
    }

}
