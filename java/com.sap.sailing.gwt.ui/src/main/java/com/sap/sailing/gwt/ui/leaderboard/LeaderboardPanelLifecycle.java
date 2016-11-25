package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class LeaderboardPanelLifecycle implements ComponentLifecycle<LeaderboardSettings, LeaderboardSettingsDialogComponent> {
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
        return new LeaderboardSettings(namesOfRaceColumnsToShow, 1000L).asDefault();
    }

    @Override
    public LeaderboardSettings cloneSettings(LeaderboardSettings settings) {
        return new LeaderboardSettings(settings.getManeuverDetailsToShow(), settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(), settings.getNamesOfRaceColumnsToShow(), settings.getNamesOfRacesToShow(), settings.getNumberOfLastRacesToShow(), settings.isAutoExpandPreSelectedRace(), settings.getDelayBetweenAutoAdvancesInMilliseconds(), settings.getNameOfRaceToSort(), settings.isSortAscending(), settings.isUpdateUponPlayStateChange(), settings.getActiveRaceColumnSelectionStrategy(), settings.isShowAddedScores(), settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn());
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public String getComponentId() {
        return "LeaderboardPanel";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
