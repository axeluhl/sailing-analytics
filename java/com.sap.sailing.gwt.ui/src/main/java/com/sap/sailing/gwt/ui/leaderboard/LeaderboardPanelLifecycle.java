package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class LeaderboardPanelLifecycle implements ComponentLifecycle<LeaderboardPanel, LeaderboardSettings, LeaderboardSettingsDialogComponent> {
    private final StringMessages stringMessages;
    private final List<RaceColumnDTO> raceList;
    private LeaderboardPanel component;
    
    public LeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.raceList = leaderboard.getRaceList();

        this.component = null;
    }
    
    @Override
    public LeaderboardSettingsDialogComponent getSettingsDialogComponent(LeaderboardSettings settings) {
        return new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(), raceList, 
                /* select all races by default */ raceList, new ExplicitRaceColumnSelection(),
                /* autoExpandPreSelectedRace */ false, settings.isShowAddedScores(),
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l, settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn(), stringMessages);
    }

    @Override
    public LeaderboardPanel getComponent() {
        return component;
    }

    @Override
    public LeaderboardSettings createDefaultSettings() {
        List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
        for (RaceColumnDTO raceColumn : raceList) {
            namesOfRaceColumnsToShow.add(raceColumn.getName());
        }
        return LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                false, /* showRegattaRank */ true, /*showCompetitorSailIdColumns*/ true,
                /*showCompetitorFullNameColumn*/ true);
    }

    @Override
    public LeaderboardSettings cloneSettings(LeaderboardSettings settings) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}

