package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
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
        return new LeaderboardSettingsDialogComponent(settings, raceList, stringMessages);
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
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.REGATTA_RANK);

        return LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, overallDetails, /* nameOfRaceToSort */null, 
                /* autoExpandPreSelectedRace */ false, 1000L, /* numberOfLastRacesToShow */null,
                /* raceColumnSelectionStrategy */ RaceColumnSelectionStrategies.EXPLICIT,
                /*showCompetitorSailIdColumns*/ true, /*showCompetitorFullNameColumn*/ true);
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

