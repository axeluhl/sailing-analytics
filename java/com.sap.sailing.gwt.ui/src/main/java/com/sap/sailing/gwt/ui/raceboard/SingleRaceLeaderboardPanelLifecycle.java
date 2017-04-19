package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.dom.client.Document;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelectionWithPreselectedRace;
import com.sap.sse.gwt.client.player.Timer;

public class SingleRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {
    
    private final boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard;
    private final Timer timer;
    private final RegattaAndRaceIdentifier raceIdentifier;

    public SingleRaceLeaderboardPanelLifecycle(Timer timer, RegattaAndRaceIdentifier raceIdentifier, StringMessages stringMessages) {
        super(null, stringMessages);
        this.raceIdentifier = raceIdentifier;
        this.timer = timer;
        this.isScreenLargeEnoughToInitiallyDisplayLeaderboard = Document.get().getClientWidth() >= 1024;
    }
    
    @Override
    public LeaderboardSettings createDefaultSettings() {
        ExplicitRaceColumnSelectionWithPreselectedRace raceColumn = new ExplicitRaceColumnSelectionWithPreselectedRace(raceIdentifier);
        LeaderboardSettings defaultLeaderboardSettingsForCurrentPlayMode = LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettingsForPlayMode(timer.getPlayMode(),
                        /* nameOfRaceToSort */ raceIdentifier.getRaceName(),
                        /* nameOfRaceColumnToShow */ null, /* nameOfRaceToShow */ raceIdentifier.getRaceName(),
                        /* showRegattaRank */ false,
                        /*showCompetitorSailIdColumn*/true,
                        /* don't showCompetitorFullNameColumn in case screen is so small that we don't
                         * even display the leaderboard initially */ isScreenLargeEnoughToInitiallyDisplayLeaderboard,raceColumn.getNumberOfLastRaceColumnsToShow(),raceColumn.getType());
        return defaultLeaderboardSettingsForCurrentPlayMode;
    }
    
    public boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard() {
        return isScreenLargeEnoughToInitiallyDisplayLeaderboard;
    }
    
    

}
