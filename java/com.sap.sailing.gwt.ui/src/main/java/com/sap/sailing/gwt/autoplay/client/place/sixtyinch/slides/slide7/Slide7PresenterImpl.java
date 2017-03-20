package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.client.CompetitorColorProvider;
import com.sap.sailing.gwt.ui.client.CompetitorColorProviderImpl;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class Slide7PresenterImpl extends ConfiguredSlideBase<Slide7Place> implements Slide7View.Slide7Presenter {
    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);
    private Slide7View view;
    private Timer raceboardTimer;
    private SailingServiceAsync sailingService;
    private AsyncActionsExecutor asyncActionsExecutor;
    private ErrorReporter errorReporter;
    private String leaderBoardName;
    private UUID eventId;
    private RaceTimesInfoProvider raceTimesInfoProvider;

    public Slide7PresenterImpl(Slide7Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide7View slide7ViewImpl) {
        super(place, clientFactory);
        this.view = slide7ViewImpl;
        sailingService = clientFactory.getSailingService();
        asyncActionsExecutor = new AsyncActionsExecutor();
        errorReporter = clientFactory.getErrorReporter();
        leaderBoardName = clientFactory.getSlideCtx().getSettings().getLeaderBoardName();
        eventId = clientFactory.getSlideCtx().getSettings().getEventId();
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        view.startingWith(this, panel);
        raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
        raceboardTimer.setLivePlayDelayInMillis(1000);
        raceboardTimer.setRefreshInterval(1000);

        raceboardTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                GWT.log("Time change " + newTime);
            }
        });

        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, errorReporter,
                new ArrayList<RegattaAndRaceIdentifier>(), 3000l);

        loadMiniLeaderBoard(new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {

            @Override
            public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> leaderBoard) {
                StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard(getSlideCtx().getEvent(),
                        leaderBoardName);

                sailingService.getCompetitorsOfLeaderboard(leaderBoardName,
                        new AsyncCallback<Iterable<CompetitorDTO>>() {

                            @Override
                            public void onSuccess(Iterable<CompetitorDTO> competitors) {
                                loadRaceTimes(selectedLeaderboard, new RaceTimesInfoProviderListener() {

                                    @Override
                                    public void raceTimesInfosReceived(
                                            Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                                            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                                            long clientTimeWhenResponseWasReceived) {

                                        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent,
                                                serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                                        raceboardTimer.play();
                                        RegattaAndRaceIdentifier lifeRace = checkForLiveRace(selectedLeaderboard,
                                                serverTimeDuringRequest);
                                        if (lifeRace != null) {
                                            sailingService.getCompetitorBoats(lifeRace,
                                                    new AsyncCallback<Map<CompetitorDTO, BoatDTO>>() {
                                                        @Override
                                                        public void onSuccess(Map<CompetitorDTO, BoatDTO> result) {
                                                            createRaceMapIfNotExist(lifeRace, selectedLeaderboard,
                                                                    result, competitors);
                                                            getEventBus().fireEvent(new SlideHeaderEvent(
                                                                    "Currently Live", lifeRace.getRaceName()));
                                                        }

                                                        @Override
                                                        public void onFailure(Throwable caught) {
                                                            view.showErrorNoLive();
                                                            getEventBus()
                                                                    .fireEvent(new SlideHeaderEvent("Currently Live",
                                                                            "Error getting Competitor Boats"));
                                                        }
                                                    });
                                        } else {
                                            view.showErrorNoLive();
                                            getEventBus().fireEvent(
                                                    new SlideHeaderEvent("Currently Live", "No Life Race Found"));
                                        }
                                    }
                                });
                            }

                    @Override
                            public void onFailure(Throwable caught) {
                                view.showErrorNoLive();
                                getEventBus()
                                        .fireEvent(new SlideHeaderEvent("Currently Live", "Error getting Competitors"));
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                view.showErrorNoLive();
                getEventBus().fireEvent(new SlideHeaderEvent("Currently Live", "Could not load Leaderboard"));
            }
        });
    }

    protected void loadRaceTimes(AbstractLeaderboardDTO selectedLeaderboard, RaceTimesInfoProviderListener callback) {
        for (RaceColumnDTO race : selectedLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                    raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                }
            }
        }
        raceTimesInfoProvider.forceTimesInfosUpdate();
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(new RaceTimesInfoProviderListener() {

            @Override
            public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                    long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                    long clientTimeWhenResponseWasReceived) {
                raceTimesInfoProvider.removeRaceTimesInfoProviderListener(this);
                callback.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                        clientTimeWhenResponseWasReceived);
            }
        });

    }

    private void loadMiniLeaderBoard(AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>> callback) {
        GetMiniLeaderbordAction leaderboardAction = new GetMiniLeaderbordAction(eventId, leaderBoardName);
        getClientFactory().getDispatch().execute(leaderboardAction, callback);
    }

    private void createRaceMapIfNotExist(RegattaAndRaceIdentifier currentLiveRace,
            StrippedLeaderboardDTO selectedLeaderboard, Map<CompetitorDTO, BoatDTO> result,
            Iterable<CompetitorDTO> competitors) {

        RaceMapSettings settings = new RaceMapSettings();
        RaceMapLifecycle raceMapLifecycle = new RaceMapLifecycle(StringMessages.INSTANCE);

        final CompetitorColorProvider colorProvider = new CompetitorColorProviderImpl(currentLiveRace,
                result);
        CompetitorSelectionModel competitorSelectionProvider = new CompetitorSelectionModel(
                /* hasMultiSelection */ true, colorProvider);
        competitorSelectionProvider.setCompetitors(competitors);
        RaceMap raceboardPerspective = new RaceMap(raceMapLifecycle, settings, sailingService, asyncActionsExecutor,
                errorReporter, raceboardTimer,
                competitorSelectionProvider, StringMessages.INSTANCE, currentLiveRace, raceMapResources, true);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceboardPerspective);
        raceboardTimer.setPlayMode(PlayModes.Live);
        view.setRaceMap(raceboardPerspective);


    }

    private RegattaAndRaceIdentifier checkForLiveRace(AbstractLeaderboardDTO currentLeaderboard,
            Date serverTimeDuringRequest) {
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceColumnDTO race : currentLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.getStartOfRace() != null
                            && raceTimes.endOfRace == null) {
                        long startTimeInMs = raceTimes.getStartOfRace().getTime();
                        long timeToSwitchBeforeRaceStartInMs = 180 * 1000;
                        long delayToLiveInMs = raceTimes.delayToLiveInMs;
                        // the switch to the live race should happen at a defined timepoint before the race start
                        // (default is 3 min)
                        if (serverTimeDuringRequest.getTime() - delayToLiveInMs > startTimeInMs
                                - timeToSwitchBeforeRaceStartInMs) {
                            return raceIdentifier;
                        }
                    }
                }
            }
        }
        return null;
    }

    private StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event, String leaderBoardName) {
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.name.equals(leaderBoardName)) {
                    return leaderboard;
                }
            }
        }
        return null;
    }
}
