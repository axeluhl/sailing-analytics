package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;
import java.util.function.Function;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.OverallLeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A viewer for a single leaderboard and a leaderboard chart.
 * @author Frank Mittag (c163874)
 *
 */
public class MultiRaceLeaderboardViewer extends AbstractLeaderboardViewer<LeaderboardPerspectiveLifecycle> {
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;
    private MultiRaceLeaderboardPanel overallLeaderboardPanel;
    
    public MultiRaceLeaderboardViewer(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> componentContext,
            LeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            final Function<String, SailingServiceAsync> sailingServiceFactory, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, 
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, DetailType chartDetailType, Iterable<DetailType> availableDetailTypes) {
        this(parent, componentContext, lifecycle, settings, new CompetitorSelectionModel(/* hasMultiSelection */true),
                sailingServiceFactory, asyncActionsExecutor, timer,
                leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, chartDetailType, availableDetailTypes);
    }

    private MultiRaceLeaderboardViewer(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> componentContext,
            LeaderboardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            CompetitorSelectionModel competitorSelectionModel,
            final Function<String, SailingServiceAsync> sailingServiceFactory, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, 
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, DetailType chartDetailType, Iterable<DetailType> availableDetailTypes) {
        super(parent, componentContext, lifecycle, settings, competitorSelectionModel, asyncActionsExecutor, timer,
                stringMessages);
        
        final SailingServiceAsync sailingServiceForMainLeaderboard = sailingServiceFactory.apply(leaderboardName);
        
        init(new MultiRaceLeaderboardPanel(this, getComponentContext(), sailingServiceForMainLeaderboard, asyncActionsExecutor,
                settings.findSettingsByComponentId(LeaderboardPanelLifecycle.ID), false,
                 competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, settings.getPerspectiveOwnSettings().isShowRaceDetails(),
                /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */ null,
                settings.getPerspectiveOwnSettings().isAutoExpandLastRaceColumn(), /* adjustTimerDelay */ true,
                /* autoApplyTopNFilter */ false, /* showCompetitorFilterStatus */ false,
                /* enableSyncScroller */ false, new ClassicLeaderboardStyle(), FlagImageResolverImpl.get(), availableDetailTypes));

        final LeaderboardPerspectiveOwnSettings perspectiveSettings = settings.getPerspectiveOwnSettings();
        final boolean showCharts = perspectiveSettings.isShowCharts();
        
        final FlowPanel mainPanel = createViewerPanel();
        initWidget(mainPanel);
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(this, getComponentContext(),
                sailingServiceForMainLeaderboard, asyncActionsExecutor, leaderboardName, chartDetailType,
                competitorSelectionProvider, timer, stringMessages, false, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);

        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiCompetitorChart);
        addChildComponent(multiCompetitorChart);

        addComponentToNavigationMenu(getLeaderboardPanel(), false, null, /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        // Remark: For now we only add the overallLeaderboardPanel to the navigation menu in case it's visible from the beginning
        
        if(showCharts) {
            multiCompetitorChart.timeChanged(timer.getTime(), null);
        }
        overallLeaderboardPanel = null;
        if (perspectiveSettings.isShowOverallLeaderboard()) {
            sailingServiceForMainLeaderboard.getOverallLeaderboardNamesContaining(leaderboardName, new MarkedAsyncCallback<List<String>>(
                    new AsyncCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> result) {
                            if (result.size() == 1) {
                                String overallLeaderboardName = result.get(0);
                                final SailingServiceAsync sailingServiceForOverallLeaderboard = sailingServiceFactory.apply(overallLeaderboardName);
                                overallLeaderboardPanel = new OverallLeaderboardPanel(MultiRaceLeaderboardViewer.this,
                                        getComponentContext(), sailingServiceForOverallLeaderboard,
                                        asyncActionsExecutor,
                                        settings.findSettingsByComponentId(OverallLeaderboardPanelLifecycle.ID),
                                        false,  competitorSelectionProvider, timer,
                                        leaderboardGroupName, overallLeaderboardName, errorReporter, stringMessages,
                                        false, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true,  /* raceTimesInfoProvider */null,
                                        false, /* adjustTimerDelay */ true, /* autoApplyTopNFilter */ false,
                                        /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false, FlagImageResolverImpl.get(), availableDetailTypes);
                                mainPanel.add(overallLeaderboardPanel);
                                addChildComponent(overallLeaderboardPanel);
                                addComponentToNavigationMenu(overallLeaderboardPanel, true, stringMessages.seriesLeaderboard(),
                                        /* hasSettingsWhenComponentIsInvisible*/ true);
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            // DO NOTHING
                        }
            }));
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if(visible) {
            timer.addTimeListener(multiCompetitorChart);
        } else {
            timer.removeTimeListener(multiCompetitorChart);
        }
    }
}
