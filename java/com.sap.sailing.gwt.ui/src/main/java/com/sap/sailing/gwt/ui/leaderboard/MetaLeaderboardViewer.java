package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.ErrorReporter;
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
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;

    
    public MetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, 
            boolean autoExpandLastRaceColumn, boolean showCharts, DetailType chartDetailType, boolean showSeriesLeaderboards) {
        this(new CompetitorSelectionModel(/* hasMultiSelection */true), sailingService, asyncActionsExecutor,  timer,
                leaderboardSettings, preselectedLeaderboardName, preselectedRace, leaderboardGroupName, metaLeaderboardName,
                errorReporter, stringMessages, userAgent, showRaceDetails, hideToolbar,
                autoExpandLastRaceColumn, showCharts, chartDetailType, showSeriesLeaderboards);
    }
    
    private MetaLeaderboardViewer(CompetitorSelectionModel competitorSelectionModel, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer, LeaderboardSettings leaderboardSettings,
            String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, boolean autoExpandLastRaceColumn,
            boolean showCharts, DetailType chartDetailType, boolean showSeriesLeaderboards) {
        super(competitorSelectionModel, asyncActionsExecutor,  timer, stringMessages, hideToolbar,
                new LeaderboardPanel(sailingService, asyncActionsExecutor,
                        leaderboardSettings, preselectedRace != null, preselectedRace, competitorSelectionModel, timer,
                        leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                        showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true,  /* raceTimesInfoProvider */null, 
                        autoExpandLastRaceColumn, /* adjustTimerDelay */ true, /*autoApplyTopNFilter*/ false, false));
        FlowPanel mainPanel = createViewerPanel();
        setWidget(mainPanel);
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, metaLeaderboardName,
                chartDetailType, competitorSelectionProvider, timer, stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);
        multiLeaderboardPanel = new MultiLeaderboardPanel(sailingService, metaLeaderboardName, asyncActionsExecutor, timer, false /*isEmbedded*/,
                preselectedLeaderboardName, preselectedRace, errorReporter, stringMessages,
                userAgent, showRaceDetails, autoExpandLastRaceColumn);
        multiLeaderboardPanel.setVisible(showSeriesLeaderboards);
        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiCompetitorChart);
        mainPanel.add(multiLeaderboardPanel);
        addComponentToNavigationMenu(getLeaderboardPanel(), false, stringMessages.seriesLeaderboard(),  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiLeaderboardPanel, true , stringMessages.regattaLeaderboards(),  /* hasSettingsWhenComponentIsInvisible*/ false);
    }

}
