package com.sap.sailing.gwt.ui.leaderboard;

import java.util.function.Function;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.MetaLeaderboardPerspectiveLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.MultipleMultiLeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class MetaLeaderboardViewer extends AbstractLeaderboardViewer<MetaLeaderboardPerspectiveLifecycle> {
    private final MultiLeaderboardProxyPanel multiLeaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;

    
    public MetaLeaderboardViewer(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> componentContext,
            MetaLeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            Function<String, SailingServiceAsync> sailingServiceFactory, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, String preselectedLeaderboardName,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, DetailType chartDetailType, Iterable<DetailType> availableDetailTypes) {
        this(parent, componentContext, lifecycle, settings, new CompetitorSelectionModel(/* hasMultiSelection */true),
                sailingServiceFactory, asyncActionsExecutor, timer,
                preselectedLeaderboardName, leaderboardGroupName, metaLeaderboardName,
                errorReporter, stringMessages, chartDetailType, availableDetailTypes);
    }
    
    private MetaLeaderboardViewer(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> componentContext,
            MetaLeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            CompetitorSelectionModel competitorSelectionModel, Function<String, SailingServiceAsync> sailingServiceFactory,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            String preselectedLeaderboardName, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            DetailType chartDetailType, Iterable<DetailType> availableDetailTypes) {
        super(parent, componentContext, lifecycle, settings, competitorSelectionModel, asyncActionsExecutor, timer,
                stringMessages);

        final SailingServiceAsync sailingServiceForMetaLeaderboard = sailingServiceFactory.apply(metaLeaderboardName);
        init(new MultiRaceLeaderboardPanel(this, componentContext, sailingServiceForMetaLeaderboard, asyncActionsExecutor,
                        settings.findSettingsByComponentId(LeaderboardPanelLifecycle.ID), /* isEmbedded */ false,
                        competitorSelectionModel, timer,
                        leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages,
                        settings.getPerspectiveOwnSettings().isShowRaceDetails(), /* competitorSearchTextBox */ null,
                        /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null,
                        settings.getPerspectiveOwnSettings().isAutoExpandLastRaceColumn(), /* adjustTimerDelay */ true,
                        /* autoApplyTopNFilter */ false,
                        /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false, new ClassicLeaderboardStyle(),
                        FlagImageResolverImpl.get(), availableDetailTypes));
        
        final LeaderboardPerspectiveOwnSettings perspectiveSettings = settings.getPerspectiveOwnSettings();
        final boolean showCharts = perspectiveSettings.isShowCharts();
        
        FlowPanel mainPanel = createViewerPanel();
        initWidget(mainPanel);
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(this, componentContext, sailingServiceForMetaLeaderboard,
                asyncActionsExecutor,
                metaLeaderboardName,
                chartDetailType, competitorSelectionProvider, timer, stringMessages, true, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);
        
        MultiRaceLeaderboardSettings leaderboardSettings = settings.findSettingsByComponentId(MultipleMultiLeaderboardPanelLifecycle.MID);
        if(leaderboardSettings == null) {
            leaderboardSettings = lifecycle.getMultiLeaderboardPanelLifecycle().createDefaultSettings();
        }

        multiLeaderboardPanel = new MultiLeaderboardProxyPanel(this, componentContext, sailingServiceFactory,
                metaLeaderboardName,
                asyncActionsExecutor, timer, false /* isEmbedded */,
                preselectedLeaderboardName,  errorReporter, stringMessages,
                perspectiveSettings.isShowRaceDetails(), perspectiveSettings.isAutoExpandLastRaceColumn(),
                leaderboardSettings, FlagImageResolverImpl.get(), availableDetailTypes);
        multiLeaderboardPanel.setVisible(perspectiveSettings.isShowSeriesLeaderboards());
        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiCompetitorChart);
        mainPanel.add(multiLeaderboardPanel);
        addComponentToNavigationMenu(getLeaderboardPanel(), false, stringMessages.seriesLeaderboard(),  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiLeaderboardPanel, true , stringMessages.regattaLeaderboards(),  /* hasSettingsWhenComponentIsInvisible*/ false);

        addChildComponent(multiLeaderboardPanel);
        addChildComponent(multiCompetitorChart);
    }

    @Override
    public String getId() {
        return null;
    }

}
