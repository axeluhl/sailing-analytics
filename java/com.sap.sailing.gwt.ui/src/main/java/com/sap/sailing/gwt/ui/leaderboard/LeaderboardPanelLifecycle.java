package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class LeaderboardPanelLifecycle
        implements ComponentLifecycle<LeaderboardSettings, LeaderboardSettingsDialogComponent> {
    protected static final String ID = "LeaderboardPanel";

    private final StringMessages stringMessages;
    private final List<RaceColumnDTO> raceList;

    public LeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.raceList = leaderboard != null ? leaderboard.getRaceList() : new ArrayList<RaceColumnDTO>();
    }

    @Override
    public LeaderboardSettingsDialogComponent getSettingsDialogComponent(LeaderboardSettings settings) {
        return new LeaderboardSettingsDialogComponent(settings, raceList, stringMessages);
    }

    @Override
    public LeaderboardSettings createDefaultSettings() {
        List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
        for (RaceColumnDTO raceColumn : raceList) {
            namesOfRaceColumnsToShow.add(raceColumn.getName());
        }
        return LeaderboardSettingsFactory.getInstance()
                .createNewSettingsWithCustomDefaults(new LeaderboardSettings(namesOfRaceColumnsToShow, 1000L));
    }

    @Override
    public LeaderboardSettings cloneSettings(LeaderboardSettings settings) {
        LeaderboardSettings clonedSettings = new LeaderboardSettings(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(),
                settings.getNamesOfRaceColumnsToShow(), settings.getNamesOfRacesToShow(),
                settings.getNumberOfLastRacesToShow(), settings.isAutoExpandPreSelectedRace(),
                settings.getDelayBetweenAutoAdvancesInMilliseconds(), settings.getNameOfRaceToSort(),
                settings.isSortAscending(), settings.isUpdateUponPlayStateChange(),
                settings.getActiveRaceColumnSelectionStrategy(), settings.isShowAddedScores(),
                settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn());
        return LeaderboardSettingsFactory.getInstance().keepDefaults(settings, clonedSettings);
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
    public LeaderboardSettings extractGlobalSettings(LeaderboardSettings currentLeaderboardSettings) {
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
        return globalLeaderboardSettings;
    }

    @Override
    public LeaderboardSettings extractContextSettings(LeaderboardSettings leaderboardSettings) {
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
        return contextSpecificLeaderboardSettings;
    }

}
