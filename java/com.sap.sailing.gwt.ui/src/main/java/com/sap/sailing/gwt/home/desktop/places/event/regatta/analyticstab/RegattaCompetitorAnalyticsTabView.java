package com.sap.sailing.gwt.home.desktop.places.event.regatta.analyticstab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts.OldCompetitorCharts;
import com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts.OldCompetitorChartsDelegateFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.SharedLeaderboardRegattaTabView;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaCompetitorAnalyticsTabView extends SharedLeaderboardRegattaTabView<RegattaCompetitorAnalyticsPlace> {
    private static final Logger logger = Logger.getLogger(RegattaCompetitorAnalyticsTabView.class.getName());
    
    interface MyBinder extends UiBinder<HTMLPanel, RegattaCompetitorAnalyticsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    private final int MAX_COMPETITORS_IN_CHART = 30; 
    private LeaderboardUpdateProvider leaderboardUpdateProvider = null;
    
    public RegattaCompetitorAnalyticsTabView() {
        super();
    }

    @UiField(provided = true)
    protected OldCompetitorCharts competitorCharts = new OldCompetitorCharts(
            ExperimentalFeatures.SHOW_COMPETITOR_ANALYTICS_FULLSCREEN_VIEWER ? new OldCompetitorChartsDelegateFullscreenViewer() : null);

    @Override
    public Class<RegattaCompetitorAnalyticsPlace> getPlaceClassForActivation() {
        return RegattaCompetitorAnalyticsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.getEventDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(RegattaCompetitorAnalyticsPlace myPlace, AcceptsOneWidget contentArea) {
        if (currentPresenter.getRegattaMetadata() == null) {
            contentArea.setWidget(new InfoPlaceholder(StringMessages.INSTANCE.noDataForEvent()));
            return;
        }

        contentArea.setWidget(currentPresenter.getErrorAndBusyClientFactory().createBusyView());
        String regattaId = currentPresenter.getRegattaId();

        if (regattaId != null && !regattaId.isEmpty()) {
            String leaderboardName = regattaId;
            RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getRegattaAnalyticsManager();
            final Runnable callback = new Runnable() {
                @Override
                public void run() {
                    leaderboardUpdateProvider = regattaAnalyticsManager.getLeaderboardPanel();
                    leaderboardUpdateProvider.addLeaderboardUpdateListener(RegattaCompetitorAnalyticsTabView.this);
                    initWidget(ourUiBinder.createAndBindUi(RegattaCompetitorAnalyticsTabView.this));

                    DetailType initialDetailType = DetailType.REGATTA_RANK;
                    if (regattaAnalyticsManager.getMultiCompetitorChart() == null) {
                        regattaAnalyticsManager.createMultiCompetitorChart(leaderboardName, initialDetailType);
                    }
                    competitorCharts.setChart(regattaAnalyticsManager.getMultiCompetitorChart(),
                            getAvailableDetailsTypes(), initialDetailType);

                    regattaAnalyticsManager.showCompetitorChart(competitorCharts.getSelectedChartDetailType());
                    contentArea.setWidget(RegattaCompetitorAnalyticsTabView.this);
                }
            };
            if (regattaAnalyticsManager.getLeaderboardPanel() == null) {
                currentPresenter.getAvailableDetailTypesForLeaderboard(leaderboardName,
                        null, new AsyncCallback<Iterable<DetailType>>() {

                            @Override
                            public void onSuccess(Iterable<DetailType> result) {
                                createSharedLeaderboardPanel(leaderboardName, regattaAnalyticsManager,
                                        currentPresenter.getUserService(), null, panel -> callback.run(), result);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                logger.log(Level.SEVERE, "Could not load detailtypes", caught);
                            }
                        });
            } else {
                callback.run();
            }
        }
    }

    private List<DetailType> getAvailableDetailsTypes() {
        List<DetailType> availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_NET_POINTS_SUM);
        return availableDetailsTypes;
    }

    @Override
    public void stop() {
    }

    @Override
    public RegattaCompetitorAnalyticsPlace placeToFire() {
        return new RegattaCompetitorAnalyticsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

    public boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // adjust the competitor selection for the chart in case the leaderboard changed
        updateCompetitorSelection();
    }

    private void updateCompetitorSelection() {
        RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx().getRegattaAnalyticsManager();
        CompetitorSelectionModel competitorSelectionProvider = regattaAnalyticsManager.getCompetitorSelectionProvider();

        // preselect the top N competitors in case there was no selection before and there too many competitors for a chart
        int competitorsCount = Util.size(competitorSelectionProvider.getAllCompetitors());
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        
        if(selectedCompetitorsCount == 0 && competitorsCount > MAX_COMPETITORS_IN_CHART) {
            List<CompetitorDTO> selectedCompetitors = new ArrayList<>();
            Iterator<CompetitorDTO> allCompetitorsIt = competitorSelectionProvider.getAllCompetitors().iterator();
            int counter = 0;
            while(counter < MAX_COMPETITORS_IN_CHART) {
                selectedCompetitors.add(allCompetitorsIt.next());
                counter++;
            }
            competitorSelectionProvider.setSelection(selectedCompetitors, (CompetitorSelectionChangeListener[]) null);
        }
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (leaderboardUpdateProvider != null) {
            leaderboardUpdateProvider.removeLeaderboardUpdateListener(this); 
        }
   }
}