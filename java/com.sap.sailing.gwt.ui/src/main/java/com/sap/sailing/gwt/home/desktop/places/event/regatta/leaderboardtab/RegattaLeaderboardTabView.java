package com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboardDelegateFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.places.Consumer;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.SharedLeaderboardRegattaTabView;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManagerWithErrorAndBusy;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.dispatch.shared.commands.CollectionResult;

public class RegattaLeaderboardTabView extends SharedLeaderboardRegattaTabView<RegattaLeaderboardPlace> {
    interface MyBinder extends UiBinder<FlowPanel, RegattaLeaderboardTabView> {
    }

    interface Style extends CssResource {
        String showLiveRacesActive();
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField Style style;
    @UiField FlowPanel buttonContainer;
    @UiField FlowPanel liveRacesContainer;
    @UiField(provided = true) LiveRacesList liveRaces;
    @UiField(provided = true) protected OldLeaderboard leaderboard;

    private LeaderboardUpdateProvider leaderboardUpdateProvider = null;
    private OldLeaderboardAndLiveRacesPresenter leaderboardAndLiveRacesPresenter;
    
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
            final RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getRegattaAnalyticsManager();
            final String leaderboardName = regattaId;
            final MultiRaceLeaderboardPanel leaderboardPanel = regattaAnalyticsManager.getLeaderboardPanel();
            final Consumer<MultiRaceLeaderboardPanel> leaderboardConsumer = new Consumer<MultiRaceLeaderboardPanel>() {
                @Override
                public void consume(MultiRaceLeaderboardPanel leaderboardPanel) {
                    leaderboardUpdateProvider = leaderboardPanel;
                    leaderboardUpdateProvider.addLeaderboardUpdateListener(RegattaLeaderboardTabView.this);
                    OldLeaderboardDelegateFullscreenViewer leaderboardDelegate = new OldLeaderboardDelegateFullscreenViewer();
                    leaderboard = new OldLeaderboard(leaderboardDelegate);
                    liveRaces = new LiveRacesList(currentPresenter, false);
                    initWidget(ourUiBinder.createAndBindUi(RegattaLeaderboardTabView.this));
                    style.ensureInjected();
                    leaderboardAndLiveRacesPresenter = new OldLeaderboardAndLiveRacesPresenter(leaderboard, liveRaces,
                            buttonContainer, liveRacesContainer, leaderboardDelegate);
                    RefreshManager refreshManager = new RefreshManagerWithErrorAndBusy(RegattaLeaderboardTabView.this,
                            contentArea, currentPresenter.getDispatch(),
                            currentPresenter.getErrorAndBusyClientFactory());
                    refreshManager.add(leaderboardAndLiveRacesPresenter.getLiveRacesRefreshableWrapper(),
                            new GetLiveRacesForRegattaAction(currentPresenter.getEventDTO().getId(), regattaId));
                    leaderboard.setLeaderboard(leaderboardPanel, currentPresenter.getAutoRefreshTimer());
                    if (currentPresenter.getEventDTO().getState() == EventState.RUNNING) {
                        // TODO: start autorefresh?
                    }
                    regattaAnalyticsManager.hideCompetitorChart();
                    contentArea.setWidget(RegattaLeaderboardTabView.this);
                    if (leaderboardPanel.getLeaderboard() != null) {
                        leaderboard.updatedLeaderboard(leaderboardPanel.getLeaderboard());
                    }

                    selectCompetitorsFromURLArgumentsWhenReady(myPlace, leaderboardPanel);
                }

                private void selectCompetitorsFromURLArgumentsWhenReady(final RegattaLeaderboardPlace myPlace,
                        MultiRaceLeaderboardPanel leaderboardPanel) {
                    // select competitors from URL arguments if present
                    leaderboardPanel.addLeaderboardUpdateListener(new LeaderboardUpdateListener() {
                        @Override
                        public void updatedLeaderboard(LeaderboardDTO leaderboard) {
                            if (!myPlace.getSelectedCompetitors().isEmpty()) {
                                Collection<CompetitorDTO> newSelection = new ArrayList<>();
                                for (String selectedCompetitorId : myPlace.getSelectedCompetitors()) {
                                    try {
                                        UUID uuid = UUID.fromString(selectedCompetitorId);
                                        for (CompetitorDTO competitor : leaderboard.competitors) {
                                            if (selectedCompetitorId.equals(competitor.getIdAsString())) {
                                                newSelection.add(competitor);
                                                break;
                                            }
                                        }
                                        //
                                    } catch (IllegalArgumentException e) {
                                        GWT.log(e.getMessage());
                                    }
                                }
                                leaderboardPanel.setSelection(newSelection);
                                if(!newSelection.isEmpty()) {
                                    leaderboardPanel.removeLeaderboardUpdateListener(this);
                                }
                            }
                        }

                        @Override
                        public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
                        }
                    });
                }
            };
            if (leaderboardPanel == null) {
                loadAvailableDetailTypes(leaderboardName, contentArea, result -> 
                createSharedLeaderboardPanel(leaderboardName, regattaAnalyticsManager,
                        currentPresenter.getUserService(), /* FIXME placeToken */ null, leaderboardConsumer, result));
            } else if ( /* FIXME placeToken not empty */ false) {
                loadAvailableDetailTypes(leaderboardName, contentArea,
                        result -> createLeaderboardComponentContext(leaderboardName, currentPresenter.getUserService(),
                                /* FIXME placeToken */ null, result).getInitialSettings(
                                        new DefaultOnSettingsLoadedCallback<MultiRaceLeaderboardSettings>() {
                                            @Override
                                            public void onSuccess(MultiRaceLeaderboardSettings settings) {
                                                leaderboardPanel.updateSettings(settings);
                                                leaderboardConsumer.consume(leaderboardPanel);
                                            }
                                        }));
            } else {
                leaderboardPanel.loadCompleteLeaderboard(false);
                leaderboardConsumer.consume(leaderboardPanel);
            }
        } else {
            showMessageLabelAndGoToHome("No leaderboard specified, cannot proceed to leaderboardpage", contentArea);
        }
    }
    
    private void loadAvailableDetailTypes(final String leaderboardName, final AcceptsOneWidget contentArea,
            final Consumer<Iterable<DetailType>> callback) {
        currentPresenter.getAvailableDetailTypesForLeaderboard(leaderboardName,
                null, new AsyncCallback<Iterable<DetailType>>() {

                    @Override
                    public void onSuccess(Iterable<DetailType> result) {
                        callback.consume(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        showMessageLabelAndGoToHome("Could not load detaillist", contentArea);
                    }
                });
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

    @UiHandler("collapseLiveRacesButton")
    void onCollapseLiveRacesButtonClicked(ClickEvent event) {
        leaderboardAndLiveRacesPresenter.setShowLiveRaces(false);
    }

    @UiHandler("fullscreenButton")
    void onFullscreenButtonClicked(ClickEvent event) {
        leaderboard.getFullscreenControl().fireEvent(event);
    }

    private class OldLeaderboardAndLiveRacesPresenter {
        private final OldLeaderboard leaderboard;
        private final LiveRacesList liveRaces;
        private final FlowPanel buttonContainer, liveRacesContainer;
        private final OldLeaderboardDelegateFullscreenViewer leaderboardDelegate;
        boolean show = false, available = false;

        private OldLeaderboardAndLiveRacesPresenter(OldLeaderboard leaderboard, LiveRacesList liveRaces,
                FlowPanel buttonContainer, FlowPanel liveRacesContainer,
                OldLeaderboardDelegateFullscreenViewer leaderboardDelegate) {
            this.leaderboard = leaderboard;
            this.liveRaces = liveRaces;
            this.buttonContainer = buttonContainer;
            this.liveRacesContainer = liveRacesContainer;
            this.leaderboardDelegate = leaderboardDelegate;

            ClickHandler showLivesRacesClickHandler = event -> setShowLiveRaces(!show);
            this.leaderboard.getShowLiveRacesControl().addClickHandler(showLivesRacesClickHandler);
            this.leaderboardDelegate.getShowLiveRacesControl().addClickHandler(showLivesRacesClickHandler);
            this.leaderboardDelegate.getShowLiveRacesControl().setTitle(StringMessages.INSTANCE.showLiveNow());

            this.leaderboardDelegate.addCloseHandler(event -> {
                liveRaces.removeFromParent();
                liveRacesContainer.add(liveRaces);
            });
            this.leaderboard.getFullscreenControl().addClickHandler(event -> {
                liveRaces.removeFromParent();
                leaderboardDelegate.setLiveRacesPanel(liveRaces);
            });
            this.setShowLiveRaces(false);
        }

        private RefreshableWidget<CollectionResult<LiveRaceDTO>> getLiveRacesRefreshableWrapper() {
            return new RefreshableWidget<CollectionResult<LiveRaceDTO>>() {
                @Override
                public void setData(CollectionResult<LiveRaceDTO> data) {
                    available = data != null && !data.getValues().isEmpty();
                    OldLeaderboardAndLiveRacesPresenter.this.updateLiveRaces();
                    liveRaces.getRefreshable().setData(data);
                }
            };
        }

        private void setShowLiveRaces(boolean show) {
            this.show = show;
            leaderboard.getShowLiveRacesControl().setStyleName(style.showLiveRacesActive(), show);
            leaderboardDelegate.getShowLiveRacesControl().setStyleName(style.showLiveRacesActive(), show);
            this.updateLiveRaces();
        }

        private void updateLiveRaces() {
            leaderboard.getShowLiveRacesControl().setVisible(!available || !show);
            leaderboard.getFullscreenControl().setVisible(!available || !show);
            buttonContainer.setVisible(available && show);
            liveRacesContainer.setVisible(available && show);
            leaderboardDelegate.setShowLiveRaces(available && show);
        }
    }
}
