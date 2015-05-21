package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldcompetitorcharts.OldCompetitorCharts;
import com.sap.sailing.gwt.home.client.place.fakeseries.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sse.common.Util;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventSeriesCompetitorAnalyticsTabView extends SharedLeaderboardEventSeriesTabView<EventSeriesCompetitorAnalyticsPlace> {
    interface MyBinder extends UiBinder<HTMLPanel, EventSeriesCompetitorAnalyticsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesView.Presenter currentPresenter;

    @UiField
    protected OldCompetitorCharts competitorCharts;

    private final int MAX_COMPETITORS_IN_CHART = 30; 

    public EventSeriesCompetitorAnalyticsTabView() {
        super();
    }

    @Override
    public Class<EventSeriesCompetitorAnalyticsPlace> getPlaceClassForActivation() {
        return EventSeriesCompetitorAnalyticsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.getCtx().getSeriesDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(EventSeriesCompetitorAnalyticsPlace myPlace, AcceptsOneWidget contentArea) {
        contentArea.setWidget(new Placeholder());
        String leaderboardName = myPlace.getCtx().getSeriesDTO().getLeaderboardId();
        if (leaderboardName != null && !leaderboardName.isEmpty()) {
            EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager = currentPresenter.getCtx().getAnalyticsManager();
            if(eventSeriesAnalyticsManager.getLeaderboardPanel() == null) {
                createSharedLeaderboardPanel(leaderboardName, eventSeriesAnalyticsManager);
            }
            initWidget(ourUiBinder.createAndBindUi(this));

            DetailType initialDetailType = DetailType.OVERALL_RANK;
            if (eventSeriesAnalyticsManager.getMultiCompetitorChart() == null) {
                eventSeriesAnalyticsManager.createMultiCompetitorChart(leaderboardName, initialDetailType);
            }
            competitorCharts.setChart(eventSeriesAnalyticsManager.getMultiCompetitorChart(), getAvailableDetailsTypes(), initialDetailType);
            eventSeriesAnalyticsManager.showCompetitorChart(competitorCharts.getSelectedChartDetailType());
            contentArea.setWidget(this);
        }
    }

    private List<DetailType> getAvailableDetailsTypes() {
        List<DetailType> availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.OVERALL_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
        return availableDetailsTypes;
    }

    @Override
    public void stop() {
    }

    @Override
    public EventSeriesCompetitorAnalyticsPlace placeToFire() {
        return new EventSeriesCompetitorAnalyticsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // adjust the competitor selection for the chart in case the leaderboard changed
        updateCompetitorSelection();
    }

    private void updateCompetitorSelection() {
        EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager = currentPresenter.getCtx().getAnalyticsManager();
        CompetitorSelectionModel competitorSelectionProvider = eventSeriesAnalyticsManager.getCompetitorSelectionProvider();

        // preselect the top N competitors in case there was no selection before and there too many competitors for a chart
        int competitorsCount = Util.size(competitorSelectionProvider.getAllCompetitors());
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        
        if(selectedCompetitorsCount == 0 && competitorsCount > MAX_COMPETITORS_IN_CHART) {
            List<CompetitorDTO> selectedCompetitors = new ArrayList<CompetitorDTO>();
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
}