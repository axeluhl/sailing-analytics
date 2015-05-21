package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

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
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.general.EventState;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends SharedLeaderboardRegattaTabView<RegattaLeaderboardPlace> {
    interface MyBinder extends UiBinder<HTMLPanel, RegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField
    protected OldLeaderboard leaderboard;

    public RegattaLeaderboardTabView() {
        super();
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
            RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx().getRegattaAnalyticsManager();
            LeaderboardPanel leaderboardPanel = regattaAnalyticsManager.getLeaderboardPanel(); 
            if(leaderboardPanel == null) {
                leaderboardPanel = createSharedLeaderboardPanel(leaderboardName, regattaAnalyticsManager);
            }
            initWidget(ourUiBinder.createAndBindUi(this));
            leaderboard.setLeaderboard(leaderboardPanel, currentPresenter.getAutoRefreshTimer());
            if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
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

    @Override
    public RegattaLeaderboardPlace placeToFire() {
        return new RegattaLeaderboardPlace(currentPresenter.getCtx());
    }
}