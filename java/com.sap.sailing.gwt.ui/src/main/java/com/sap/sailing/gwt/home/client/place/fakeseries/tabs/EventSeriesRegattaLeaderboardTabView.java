package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard.OldMultiLeaderboard;
import com.sap.sailing.gwt.home.client.place.event.seriesanalytics.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.event2.EventParamUtils;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class EventSeriesRegattaLeaderboardTabView extends Composite implements SeriesTabView<EventSeriesRegattaLeaderboardPlace>,
        LeaderboardUpdateListener {

    private SeriesView.Presenter currentPresenter;

    @UiField
    protected OldMultiLeaderboard leaderboard;


    public EventSeriesRegattaLeaderboardTabView() {

    }

    @Override
    public Class<EventSeriesRegattaLeaderboardPlace> getPlaceClassForActivation() {
        return EventSeriesRegattaLeaderboardPlace.class;
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(final EventSeriesRegattaLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String leaderboardName = myPlace.getCtx().getSeriesDTO().getLeaderboardId();

        if (leaderboardName != null && !leaderboardName.isEmpty()) {
          
            EventSeriesAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getAnalyticsManager();


            boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                    LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);

            final LeaderboardSettings leaderboardSettings = EventParamUtils
                    .createLeaderboardSettingsFromURLParameters(Window.Location
                    .getParameterMap());

            final RaceIdentifier preselectedRace = EventParamUtils
                    .getPreselectedRace(Window.Location.getParameterMap());

            MultiLeaderboardPanel leaderboardPanel = regattaAnalyticsManager.createMultiLeaderboardPanel(leaderboardSettings,
                    null, // TODO: preselectedLeaderboardName
                    preselectedRace,
                    "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                    currentPresenter.getCtx().getSeriesDTO().getLeaderboardId(),
                    true, // this information came from place, now hard coded. check with frank
                    autoExpandLastRaceColumn);

            initWidget(ourUiBinder.createAndBindUi(this));

            leaderboard.setMultiLeaderboard(leaderboardPanel,
                    currentPresenter.getAutoRefreshTimer());

            leaderboardPanel.addLeaderboardUpdateListener(this);

            if (currentPresenter.getCtx().getSeriesDTO().getState() != EventSeriesState.RUNNING) {
                // TODO: this.leaderboard.hideRefresh();
            } else {
                // TODO: start autorefresh?
            }


            regattaAnalyticsManager.hideCompetitorChart();

            contentArea.setWidget(this);
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
        this.leaderboard.updatedMultiLeaderboard(leaderboard, true); // TODO hard coded
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {

    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, EventSeriesRegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public EventSeriesRegattaLeaderboardPlace placeToFire() {
        return new EventSeriesRegattaLeaderboardPlace(currentPresenter.getCtx());
    }



}