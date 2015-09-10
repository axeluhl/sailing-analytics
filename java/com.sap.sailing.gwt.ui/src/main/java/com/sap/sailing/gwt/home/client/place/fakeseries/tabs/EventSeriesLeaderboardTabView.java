package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboardDelegateFullscreenViewer;
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.client.place.fakeseries.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.shared.partials.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventSeriesLeaderboardTabView extends SharedLeaderboardEventSeriesTabView<EventSeriesOverallLeaderboardPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, EventSeriesLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private SeriesView.Presenter currentPresenter;

    @UiField(provided = true)
    protected OldLeaderboard leaderboard;

    public EventSeriesLeaderboardTabView() {
        leaderboard = new OldLeaderboard(new OldLeaderboardDelegateFullscreenViewer());
    }

    @Override
    public Class<EventSeriesOverallLeaderboardPlace> getPlaceClassForActivation() {
        return EventSeriesOverallLeaderboardPlace.class;
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.getCtx().getSeriesDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(final EventSeriesOverallLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {
        contentArea.setWidget(new Placeholder());
        String leaderboardName = myPlace.getCtx().getSeriesDTO().getLeaderboardId();
        if (leaderboardName != null && !leaderboardName.isEmpty()) {
            EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager = currentPresenter.getCtx().getAnalyticsManager();
            LeaderboardPanel leaderboardPanel = eventSeriesAnalyticsManager.getLeaderboardPanel(); 
            if(leaderboardPanel == null) {
                leaderboardPanel = createSharedLeaderboardPanel(leaderboardName, eventSeriesAnalyticsManager);
            }
            initWidget(ourUiBinder.createAndBindUi(this));
            leaderboard.setLeaderboard(leaderboardPanel, currentPresenter.getAutoRefreshTimer());
            eventSeriesAnalyticsManager.hideCompetitorChart();
            contentArea.setWidget(this);
            if(leaderboardPanel.getLeaderboard() != null) {
                leaderboard.updatedLeaderboard(leaderboardPanel.getLeaderboard());
            }
        } else {
            contentArea.setWidget(new Label("No leaderboard specified, cannot proceed to leaderboardpage"));
            new com.google.gwt.user.client.Timer() {
                @Override
                public void run() {
                    currentPresenter.getHomeNavigation().goToPlace();
                }
            }.schedule(3000);
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard.updatedLeaderboard(leaderboard);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
    }

    @Override
    public void stop() {
    }

    @Override
    public EventSeriesOverallLeaderboardPlace placeToFire() {
        return new EventSeriesOverallLeaderboardPlace(currentPresenter.getCtx());
    }
}