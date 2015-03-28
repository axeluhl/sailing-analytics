package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelection;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsDialogComponent;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;

public class LeaderboardSettingsDialog extends SettingsDialog<LeaderboardSettings> {

    public LeaderboardSettingsDialog(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard, DialogCallback<LeaderboardSettings> callback) {
        super(new ProxyLeaderboardComponent(stringMessages, leaderboard), stringMessages, callback);
    }
    
    public static class ProxyLeaderboardComponent implements Component<LeaderboardSettings> {
        private final StringMessages stringMessages;
        private final LeaderboardSettingsDialogComponent settingsDialogComponent;
        
        public ProxyLeaderboardComponent(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
            this.stringMessages = stringMessages;
            List<RaceColumnDTO> raceList = leaderboard.getRaceList();
            List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
            for (RaceColumnDTO raceColumn : raceList) {
                namesOfRaceColumnsToShow.add(raceColumn.getName());
            }
            LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                    namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                    false, /* showRegattaRank */ true);
            this.settingsDialogComponent = new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(), raceList, 
                /* select all races by default */ raceList, new ExplicitRaceColumnSelection(),
                /* autoExpandPreSelectedRace */ false, settings.isShowAddedScores(),
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l, settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn(), stringMessages);
        }

        @Override
        public boolean hasSettings() {
            return true;
        }

        @Override
        public SettingsDialogComponent<LeaderboardSettings> getSettingsDialogComponent() {
            return settingsDialogComponent;
        }

        @Override
        public void updateSettings(LeaderboardSettings newSettings) {
            // no-op; the resulting URL has already been updated to the anchor in the dialog
        }

        @Override
        public String getLocalizedShortName() {
            return stringMessages.leaderboardConfiguration();
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
    }    
}
