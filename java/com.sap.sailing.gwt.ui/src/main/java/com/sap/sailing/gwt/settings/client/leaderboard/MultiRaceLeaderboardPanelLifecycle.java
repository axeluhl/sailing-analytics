package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

public class MultiRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle<MultiRaceLeaderboardSettings> {
    public MultiRaceLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super(leaderboard, stringMessages);
    }
    @Override
    public MultiRaceLeaderboardSettings extractUserSettings(MultiRaceLeaderboardSettings currentLeaderboardSettings) {
        MultiRaceLeaderboardSettings defaultLeaderboardSettings = createDefaultSettings();
        MultiRaceLeaderboardSettings globalLeaderboardSettings = new MultiRaceLeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                defaultLeaderboardSettings.getNamesOfRaceColumnsToShow(),
                currentLeaderboardSettings.getNumberOfLastRacesToShow(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                currentLeaderboardSettings.isUpdateUponPlayStateChange(),
                currentLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality());
        return SettingsUtil.copyValues(globalLeaderboardSettings, defaultLeaderboardSettings);
    }
    
    @Override
    public MultiRaceLeaderboardSettings extractDocumentSettings(MultiRaceLeaderboardSettings currentLeaderboardSettings) {
        MultiRaceLeaderboardSettings defaultLeaderboardSettings = createDefaultSettings();
        MultiRaceLeaderboardSettings contextSpecificLeaderboardSettings = new MultiRaceLeaderboardSettings(
                currentLeaderboardSettings.getManeuverDetailsToShow(), currentLeaderboardSettings.getLegDetailsToShow(),
                currentLeaderboardSettings.getRaceDetailsToShow(), currentLeaderboardSettings.getOverallDetailsToShow(),
                defaultLeaderboardSettings.getNamesOfRaceColumnsToShow(),
                currentLeaderboardSettings.getNumberOfLastRacesToShow(),
                currentLeaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                currentLeaderboardSettings.isUpdateUponPlayStateChange(),
                currentLeaderboardSettings.getActiveRaceColumnSelectionStrategy(),
                currentLeaderboardSettings.isShowAddedScores(),
                currentLeaderboardSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                currentLeaderboardSettings.isShowCompetitorSailIdColumn(),
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality());
        return SettingsUtil.copyValues(contextSpecificLeaderboardSettings, defaultLeaderboardSettings);
    }
    
    @Override
    public MultiRaceLeaderboardSettingsDialogComponent getSettingsDialogComponent(MultiRaceLeaderboardSettings settings) {
        return new MultiRaceLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages);
    }

    @Override
    public MultiRaceLeaderboardSettings createDefaultSettings() {
        return LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettingsWithRaceColumns(namesOfRaceColumns);
    }
}
