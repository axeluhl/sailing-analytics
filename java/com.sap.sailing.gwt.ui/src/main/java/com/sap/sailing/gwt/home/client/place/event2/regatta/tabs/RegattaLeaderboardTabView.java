package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

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
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.client.place.event.regattaanalytics.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event2.utils.EventParamUtils;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends Composite implements RegattaTabView<RegattaLeaderboardPlace>,
        LeaderboardUpdateListener {

    private Presenter currentPresenter;

    @UiField
    protected OldLeaderboard leaderboard;


    public RegattaLeaderboardTabView() {

    }

    @Override
    public Class<RegattaLeaderboardPlace> getPlaceClassForActivation() {
        return RegattaLeaderboardPlace.class;
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.getCtx().getEventDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(final RegattaLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String regattaId = myPlace.getRegattaId();

        if (regattaId != null && !regattaId.isEmpty()) {

            String leaderboardName = regattaId;
          
            RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getRegattaAnalyticsManager();


            boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                    LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);

            final LeaderboardSettings leaderboardSettings = EventParamUtils
                    .createLeaderboardSettingsFromURLParameters(Window.Location
                    .getParameterMap());

            final RaceIdentifier preselectedRace = EventParamUtils
                    .getPreselectedRace(Window.Location.getParameterMap());


            LeaderboardPanel leaderboardPanel = regattaAnalyticsManager.createLeaderboardPanel( //
                    leaderboardSettings, //
                    preselectedRace, //
                    "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                    leaderboardName, //
                    true, // this information came from place, now hard coded. check with frank
                    autoExpandLastRaceColumn);

            initWidget(ourUiBinder.createAndBindUi(this));

            leaderboard.setLeaderboard(leaderboardPanel,
                    currentPresenter.getAutoRefreshTimer());

            leaderboardPanel.addLeaderboardUpdateListener(this);

            if (currentPresenter.getCtx().getEventDTO().getState() != EventState.RUNNING) {

                this.leaderboard.hideRefresh();
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
        this.leaderboard.updatedLeaderboard(leaderboard, true);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {

    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaLeaderboardPlace placeToFire() {
        return new RegattaLeaderboardPlace(currentPresenter.getCtx());
    }

}