package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class LeaderboardPanelLifecycle
        implements ComponentLifecycle<LeaderboardSettings> {
    public static final String ID = "lb";

    protected final StringMessages stringMessages;
    protected final List<String> namesOfRaceColumns;

    public LeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.namesOfRaceColumns = leaderboard != null ? leaderboard.getNamesOfRaceColumns() : new ArrayList<String>();
    }

    @Override
    public LeaderboardSettingsDialogComponent getSettingsDialogComponent(LeaderboardSettings settings) {
        return new LeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages);
    }

    @Override
    public LeaderboardSettings createDefaultSettings() {
        return LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettingsWithRaceColumns(namesOfRaceColumns);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public LeaderboardSettings extractUserSettings(LeaderboardSettings currentLeaderboardSettings) {
        LeaderboardSettings defaultLeaderboardSettings = SettingsDefaultValuesUtils.getDefaultSettings(new LeaderboardSettings(), currentLeaderboardSettings);
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
                currentLeaderboardSettings.isShowCompetitorFullNameColumn(),
                currentLeaderboardSettings.isShowCompetitorNationality());
        SettingsDefaultValuesUtils.keepDefaults(currentLeaderboardSettings, globalLeaderboardSettings);
        return globalLeaderboardSettings;
    }

    @Override
    public LeaderboardSettings extractDocumentSettings(LeaderboardSettings leaderboardSettings) {
        return leaderboardSettings;
    }

}
