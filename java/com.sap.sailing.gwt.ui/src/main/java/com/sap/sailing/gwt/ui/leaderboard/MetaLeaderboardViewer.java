package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class MetaLeaderboardViewer extends AbstractLeaderboardViewer<MetaLeaderboardPerspectiveLifecycle> {
    private final MultiLeaderboardPanel multiLeaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;

    
    public MetaLeaderboardViewer(Component<?> parent, MetaLeaderboardComponentContext componentContext,
            MetaLeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, String preselectedLeaderboardName, RegattaAndRaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, DetailType chartDetailType) {
        this(parent, componentContext, lifecycle, settings, new CompetitorSelectionModel(/* hasMultiSelection */true),
                sailingService, asyncActionsExecutor, timer,
                preselectedLeaderboardName, preselectedRace, leaderboardGroupName, metaLeaderboardName,
                errorReporter, stringMessages, userAgent, chartDetailType);
    }
    
    private MetaLeaderboardViewer(Component<?> parent, MetaLeaderboardComponentContext componentContext,
            MetaLeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            CompetitorSelectionModel competitorSelectionModel, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            String preselectedLeaderboardName, RegattaAndRaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, DetailType chartDetailType) {
        super(parent, componentContext, lifecycle, settings, competitorSelectionModel, asyncActionsExecutor, timer,
                stringMessages);
        /**
         * Cleanup one java8 suppliers can be used
         */
        init(new LeaderboardPanel(this, sailingService, asyncActionsExecutor,
                        settings.findSettingsByComponentId(LeaderboardPanelLifecycle.ID),
                        preselectedRace != null, preselectedRace, competitorSelectionModel, timer,
                        leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                        settings.getPerspectiveOwnSettings().isShowRaceDetails(), /* competitorSearchTextBox */ null,
                        /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null,
                        settings.getPerspectiveOwnSettings().isAutoExpandLastRaceColumn(), /* adjustTimerDelay */ true,
                        /* autoApplyTopNFilter */ false,
                        /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false));
        final LeaderboardPerspectiveOwnSettings perspectiveSettings = settings.getPerspectiveOwnSettings();
        final boolean showCharts = perspectiveSettings.isShowCharts();
        
        FlowPanel mainPanel = createViewerPanel();
        initWidget(mainPanel);
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(this, sailingService, asyncActionsExecutor,
                metaLeaderboardName,
                chartDetailType, competitorSelectionProvider, timer, stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);
        multiLeaderboardPanel = new MultiLeaderboardPanel(this, sailingService, metaLeaderboardName,
                asyncActionsExecutor, timer, false /* isEmbedded */,
                preselectedLeaderboardName, preselectedRace, errorReporter, stringMessages,
                userAgent, perspectiveSettings.isShowRaceDetails(), perspectiveSettings.isAutoExpandLastRaceColumn());
        multiLeaderboardPanel.setVisible(perspectiveSettings.isShowSeriesLeaderboards());
        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiCompetitorChart);
        mainPanel.add(multiLeaderboardPanel);
        addComponentToNavigationMenu(getLeaderboardPanel(), false, stringMessages.seriesLeaderboard(),  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiLeaderboardPanel, true , stringMessages.regattaLeaderboards(),  /* hasSettingsWhenComponentIsInvisible*/ false);
    }

}
