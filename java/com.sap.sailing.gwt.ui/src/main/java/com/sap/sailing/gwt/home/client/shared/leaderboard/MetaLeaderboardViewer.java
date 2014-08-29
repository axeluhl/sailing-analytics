package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class MetaLeaderboardViewer extends AbstractLeaderboardViewer {    
    private final MultiLeaderboardPanel multiLeaderboardPanel;
    
    public MetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        this(new CompetitorSelectionModel(/* hasMultiSelection */true), sailingService, asyncActionsExecutor,  timer,
                leaderboardSettings, preselectedLeaderboardName, preselectedRace, leaderboardGroupName, metaLeaderboardName,
                errorReporter, stringMessages, userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
    }
    
    private MetaLeaderboardViewer(CompetitorSelectionModel competitorSelectionModel, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer, LeaderboardSettings leaderboardSettings,
            String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        super(competitorSelectionModel, asyncActionsExecutor,  timer, stringMessages, new LeaderboardPanel(sailingService, asyncActionsExecutor,
                        leaderboardSettings, true, preselectedRace, competitorSelectionModel, timer,
                        leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                        showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, 
                        /* adjustTimerDelay */ true, /*autoApplyTopNFilter*/false, false));
        final FlowPanel mainPanel = new FlowPanel();
        setWidget(mainPanel);
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");
        multiLeaderboardPanel = new MultiLeaderboardPanel(sailingService, metaLeaderboardName, asyncActionsExecutor, timer,
                preselectedLeaderboardName, preselectedRace, errorReporter, stringMessages,
                userAgent, showRaceDetails, autoExpandLastRaceColumn);
        multiLeaderboardPanel.setVisible(showSeriesLeaderboards);
        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiLeaderboardPanel);
    }

}
