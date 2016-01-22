package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.ComponentConstructionParameters;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class LeaderboardPanelLifecycle implements ComponentLifecycle<LeaderboardPanel, LeaderboardSettings, LeaderboardSettingsDialogComponent, LeaderboardPanelLifecycle.ConstructorArgs> {
    private final StringMessages stringMessages;
    private final List<RaceColumnDTO> raceList;

    public static class ConstructionParameters extends ComponentConstructionParameters<LeaderboardPanel, LeaderboardSettings, LeaderboardSettingsDialogComponent, LeaderboardPanelLifecycle.ConstructorArgs> {
        public ConstructionParameters(LeaderboardPanelLifecycle componentLifecycle,
                ConstructorArgs componentConstructorArgs, LeaderboardSettings settings) {
            super(componentLifecycle, componentConstructorArgs, settings);
        }
    }
    
    public LeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.raceList = leaderboard.getRaceList();
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
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.REGATTA_RANK);

        return LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(namesOfRaceColumnsToShow, /* namesOfRacesToShow */
                null, overallDetails, /* nameOfRaceToSort */null,
                /* autoExpandPreSelectedRace */false, 1000L, /* numberOfLastRacesToShow */null,
                /* raceColumnSelectionStrategy */RaceColumnSelectionStrategies.EXPLICIT,
                /* showCompetitorSailIdColumns */true, /* showCompetitorFullNameColumn */true);
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

    @Override
    public LeaderboardPanel createComponent(ConstructorArgs componentConstructorArgs,
            LeaderboardSettings settings) {
        return componentConstructorArgs.createComponent(settings);
    }

    public interface ConstructorArgs extends ComponentConstructorArgs<LeaderboardPanel, LeaderboardSettings> {
    };

    public static class ConstructorArgsV1 implements ConstructorArgs {
        private final SailingServiceAsync sailingService;
        private final AsyncActionsExecutor asyncActionsExecutor;
        private final LeaderboardSettings settings; 
        private final boolean isEmbedded; 
        private final RegattaAndRaceIdentifier preSelectedRace;
        private final CompetitorSelectionProvider competitorSelectionProvider; 
        private final Timer timer; 
        private final String leaderboardGroupName;
        private final String leaderboardName; 
        private final ErrorReporter errorReporter; 
        private final StringMessages stringMessages;
        private final UserAgentDetails userAgent;
        private final boolean showRaceDetails; 
        
        public ConstructorArgsV1(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
                LeaderboardSettings settings, boolean isEmbedded, RegattaAndRaceIdentifier preSelectedRace,
                CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardGroupName,
                String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages,
                final UserAgentDetails userAgent, boolean showRaceDetails) {
            this.sailingService = sailingService;
            this.asyncActionsExecutor = asyncActionsExecutor;
            this.settings = settings;
            this.isEmbedded = isEmbedded;
            this.preSelectedRace = preSelectedRace;
            this.competitorSelectionProvider = competitorSelectionProvider;
            this.timer = timer;
            this.leaderboardGroupName = leaderboardGroupName;
            this.leaderboardName = leaderboardName;
            this.errorReporter = errorReporter;
            this.stringMessages = stringMessages;
            this.userAgent = userAgent;
            this.showRaceDetails = showRaceDetails;
        }

        @Override
        public LeaderboardPanel createComponent(LeaderboardSettings newSettings) {
            LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                    settings, isEmbedded, preSelectedRace,
                    competitorSelectionProvider, timer, leaderboardGroupName,
                    leaderboardName, errorReporter, stringMessages,
                    userAgent, showRaceDetails, /* competitorSearchTextBox */ null, /* showRegattaRank */
                    /* showSelectionCheckbox */false, /* raceTimesInfoProvider */null, false, /* autoExpandLastRaceColumn */
                    /* adjustTimerDelay */true, /*autoApplyTopNFilter*/ false, false);
            if (newSettings != null) {
                leaderboardPanel.updateSettings(newSettings);
            }
            return leaderboardPanel;
        }
    }
}
