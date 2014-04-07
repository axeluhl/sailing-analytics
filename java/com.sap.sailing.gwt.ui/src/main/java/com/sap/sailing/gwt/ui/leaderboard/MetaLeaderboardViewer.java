package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for an overall series leaderboard.
 * Additionally the viewer can render a chart for the series leaderboard and a MultiLeaderboardPanel
 * where the user can select to show one leaderboard of the series.  
 * @author Frank Mittag (c163874)
 */
public class MetaLeaderboardViewer extends AbstractLeaderboardViewer {    
    private final LeaderboardPanel metaLeaderboardPanel;
    private final MultiLeaderboardPanel multiLeaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;

    public MetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, boolean autoExpandLastRaceColumn, 
            boolean showCharts, DetailType chartDetailType, boolean showSeriesLeaderboards) {
        super(new CompetitorSelectionModel(/* hasMultiSelection */true), asyncActionsExecutor, 
                new Timer(PlayModes.Replay, /*delayBetweenAutoAdvancesInMilliseconds*/ 3000l), stringMessages, hideToolbar);

        FlowPanel mainPanel = createViewerPanel();
        setWidget(mainPanel);
        
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");

        metaLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, preselectedRace, competitorSelectionProvider, timer,
                leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                showRaceDetails, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn,  /* adjustTimerDelay */ true);

        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, metaLeaderboardName,
                chartDetailType, competitorSelectionProvider, timer, stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);
        
        multiLeaderboardPanel = new MultiLeaderboardPanel(sailingService, metaLeaderboardName, asyncActionsExecutor, timer,
                preselectedLeaderboardName, preselectedRace, errorReporter, stringMessages,
                userAgent, showRaceDetails, autoExpandLastRaceColumn);
        multiLeaderboardPanel.setVisible(showSeriesLeaderboards);
        
        mainPanel.add(metaLeaderboardPanel);
        mainPanel.add(multiCompetitorChart);
        mainPanel.add(multiLeaderboardPanel);

        addComponentToNavigationMenu(metaLeaderboardPanel, false, stringMessages.seriesLeaderboard(),  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiLeaderboardPanel, true , stringMessages.regattaLeaderboards(),  /* hasSettingsWhenComponentIsInvisible*/ false);
    }
}
