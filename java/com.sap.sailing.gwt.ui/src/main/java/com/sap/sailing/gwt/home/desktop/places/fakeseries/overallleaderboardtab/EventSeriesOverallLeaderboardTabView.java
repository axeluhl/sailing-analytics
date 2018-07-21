package com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboardDelegateFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.places.Consumer;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.SharedLeaderboardEventSeriesTabView;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventSeriesOverallLeaderboardTabView extends SharedLeaderboardEventSeriesTabView<EventSeriesOverallLeaderboardPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, EventSeriesOverallLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private SeriesView.Presenter currentPresenter;

    @UiField(provided = true)
    protected OldLeaderboard leaderboard;

    public EventSeriesOverallLeaderboardTabView() {
        leaderboard = new OldLeaderboard(new OldLeaderboardDelegateFullscreenViewer(false));
        leaderboard.getShowLiveRacesControl().removeFromParent();
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
        return currentPresenter.getSeriesDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @SuppressWarnings("unused")
    @Override
    public void start(final EventSeriesOverallLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {
        contentArea.setWidget(currentPresenter.getErrorAndBusyClientFactory().createBusyView());
        String leaderboardName = currentPresenter.getSeriesDTO().getLeaderboardId();
        if (leaderboardName != null && !leaderboardName.isEmpty()) {
            currentPresenter.getCtx().getAnalyticsManager().getAvailableDetailTypesForLeaderboard(leaderboardName, null, new AsyncCallback<Iterable<DetailType>>() {
                
                @Override
                public void onSuccess(Iterable<DetailType> result) {
                    EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager = currentPresenter.getCtx().getAnalyticsManager();
                    final Consumer<MultiRaceLeaderboardPanel> leaderboardConsumer = new Consumer<MultiRaceLeaderboardPanel>() {
                        @Override
                        public void consume(MultiRaceLeaderboardPanel leaderboardPanel) {
                            initWidget(ourUiBinder.createAndBindUi(EventSeriesOverallLeaderboardTabView.this));
                            leaderboard.setLeaderboard(leaderboardPanel, currentPresenter.getAutoRefreshTimer());
                            eventSeriesAnalyticsManager.hideCompetitorChart();
                            contentArea.setWidget(EventSeriesOverallLeaderboardTabView.this);
                            if(leaderboardPanel.getLeaderboard() != null) {
                                leaderboard.updatedLeaderboard(leaderboardPanel.getLeaderboard());
                            }
                        }
                    };
                    MultiRaceLeaderboardPanel leaderboardPanel = eventSeriesAnalyticsManager.getLeaderboardPanel(); 
                    if(leaderboardPanel == null) {
                        createSharedLeaderboardPanel(leaderboardName, eventSeriesAnalyticsManager, currentPresenter.getUserService(), /*FIXME placeToken */ null, leaderboardConsumer, result);
                    } else if( /*FIXME placeToken not empty */ false) {
                        createLeaderboardComponentContext(leaderboardName, currentPresenter.getUserService(), /*FIXME placeToken */ null, result).getInitialSettings(new DefaultOnSettingsLoadedCallback<MultiRaceLeaderboardSettings>() {
                            @Override
                            public void onSuccess(MultiRaceLeaderboardSettings settings) {
                                leaderboardPanel.updateSettings(settings);
                                leaderboardConsumer.consume(leaderboardPanel);
                            }
                        });
                    } else {
                        leaderboardConsumer.consume(leaderboardPanel);
                    }                    
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    showMessageLabelAndGoToHome("Could not load detailType list", contentArea);
                }
            });
        } else {
            showMessageLabelAndGoToHome("No leaderboard specified, cannot proceed to leaderboardpage", contentArea);
        }
    }

    private void showMessageLabelAndGoToHome(final String message, final AcceptsOneWidget contentArea) {
        contentArea.setWidget(new Label(message));
        new Timer() {
            @Override
            public void run() {
                currentPresenter.getHomeNavigation().goToPlace();
            }
        }.schedule(3000);
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