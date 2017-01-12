package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;

public abstract class AbstractLeaderboardComponentContext<L extends AbstractLeaderboardPerspectiveLifecycle> extends
        AbstractComponentContextWithSettingsStorage<L, PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> {

    public AbstractLeaderboardComponentContext(L rootLifecycle,
            SettingsStorageManager<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
    }

    @Override
    protected PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> newRootSettings) {
        // TODO extract common logic (copied from RaceBoard)
        String leaderboardComponentId = rootLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings leaderboardSettings = (LeaderboardSettings) newRootSettings.getSettingsPerComponentId()
                .get(leaderboardComponentId);
        PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> defaultSettings = rootLifecycle
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
        contextSpecificLeaderboardSettings = LeaderboardSettingsFactory.getInstance().keepDefaults(leaderboardSettings,
                contextSpecificLeaderboardSettings);

        Map<String, Settings> clonedComponentIdsAndSettings = rootLifecycle
                .cloneComponentIdsAndSettings(defaultSettings);
        clonedComponentIdsAndSettings.put(leaderboardComponentId, contextSpecificLeaderboardSettings);
        return new PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>(
                defaultSettings.getPerspectiveOwnSettings(), clonedComponentIdsAndSettings);
    }

    @Override
    protected PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> newRootSettings) {
        // TODO extract common logic (copied from RaceBoard)
        String leaderboardComponentId = rootLifecycle.getLeaderboardPanelLifecycle().getComponentId();
        LeaderboardSettings currentLeaderboardSettings = (LeaderboardSettings) newRootSettings
                .getSettingsPerComponentId().get(leaderboardComponentId);
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
        globalLeaderboardSettings = LeaderboardSettingsFactory.getInstance().keepDefaults(currentLeaderboardSettings,
                globalLeaderboardSettings);
        Map<String, Settings> clonedComponentIdsAndSettings = rootLifecycle
                .cloneComponentIdsAndSettings(newRootSettings);
        clonedComponentIdsAndSettings.put(leaderboardComponentId, globalLeaderboardSettings);
        return new PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>(
                newRootSettings.getPerspectiveOwnSettings(), clonedComponentIdsAndSettings);
    }
}
