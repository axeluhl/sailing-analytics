package com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab;

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
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboardDelegateFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.places.Consumer;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.SharedLeaderboardRegattaTabView;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends SharedLeaderboardRegattaTabView<RegattaLeaderboardPlace> {
    interface MyBinder extends UiBinder<HTMLPanel, RegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField(provided = true)
    protected OldLeaderboard leaderboard;

    private LeaderboardUpdateProvider leaderboardUpdateProvider = null;
    
    public RegattaLeaderboardTabView() {
        leaderboard = new OldLeaderboard(new OldLeaderboardDelegateFullscreenViewer());
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
        return currentPresenter.getEventDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @SuppressWarnings("unused")
    @Override
    public void start(final RegattaLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {
        if(currentPresenter.getRegattaMetadata() == null) {
            contentArea.setWidget(new InfoPlaceholder(StringMessages.INSTANCE.noDataForEvent()));
            return;
        }
        
        contentArea.setWidget(currentPresenter.getErrorAndBusyClientFactory().createBusyView());
        String regattaId = currentPresenter.getRegattaId();
        if (regattaId != null && !regattaId.isEmpty()) {
            String leaderboardName = regattaId;
            RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx().getRegattaAnalyticsManager();
            LeaderboardPanel leaderboardPanel = regattaAnalyticsManager.getLeaderboardPanel();
            final Consumer<LeaderboardPanel> leaderboardConsumer = new Consumer<LeaderboardPanel>() {
                @Override
                public void consume(LeaderboardPanel leaderboardPanel) {
                    leaderboardUpdateProvider = leaderboardPanel;
                    leaderboardUpdateProvider.addLeaderboardUpdateListener(RegattaLeaderboardTabView.this);
                    initWidget(ourUiBinder.createAndBindUi(RegattaLeaderboardTabView.this));
                    leaderboard.setLeaderboard(leaderboardPanel, currentPresenter.getAutoRefreshTimer());
                    if (currentPresenter.getEventDTO().getState() == EventState.RUNNING) {
                        // TODO: start autorefresh?
                    }
                    regattaAnalyticsManager.hideCompetitorChart();
                    contentArea.setWidget(RegattaLeaderboardTabView.this);
                    if(leaderboardPanel.getLeaderboard() != null) {
                        leaderboard.updatedLeaderboard(leaderboardPanel.getLeaderboard());
                    }
                }
            };
            if(leaderboardPanel == null) {
                createSharedLeaderboardPanel(leaderboardName, regattaAnalyticsManager, currentPresenter.getUserService(), /*FIXME placeToken */ null, leaderboardConsumer);
            } else if( /*FIXME placeToken not empty */ false) {
                createLeaderboardComponentContext(leaderboardName, currentPresenter.getUserService(), /*FIXME placeToken */ null).getInitialSettings(new DefaultOnSettingsLoadedCallback<LeaderboardSettings>() {
                    @Override
                    public void onSuccess(LeaderboardSettings settings) {
                        leaderboardPanel.updateSettings(settings);
                        leaderboardConsumer.consume(leaderboardPanel);
                    }
                });
            } else {
                leaderboardConsumer.consume(leaderboardPanel);
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
    public RegattaLeaderboardPlace placeToFire() {
        return new RegattaLeaderboardPlace(currentPresenter.getCtx());
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        if (leaderboardUpdateProvider != null) {
            leaderboardUpdateProvider.removeLeaderboardUpdateListener(this);
        }
    }
}