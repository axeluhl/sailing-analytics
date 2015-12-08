package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelection;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ProxyLeaderboardComponent implements Component<LeaderboardSettings> {
    private final StringMessages stringMessages;
    private LeaderboardSettings settings;
    private final List<RaceColumnDTO> raceList;
    
    public ProxyLeaderboardComponent(LeaderboardSettings settings, AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
        raceList = leaderboard.getRaceList();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardSettings> getSettingsDialogComponent() {
        return new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(), raceList, 
                /* select all races by default */ raceList, new ExplicitRaceColumnSelection(),
                /* autoExpandPreSelectedRace */ false, settings.isShowAddedScores(),
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l, settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn(), stringMessages);
    }

    @Override
    public void updateSettings(LeaderboardSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public Widget getEntryWidget() {
        throw new UnsupportedOperationException(
                "Internal error. This settings dialog does not actually belong to a LeaderboardPanel");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "leaderboardSettingsDialog";
    }

    @Override
    public LeaderboardSettings getSettings() {
        return settings;
    }
}