package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.CompetitorColorProvider;
import com.sap.sailing.gwt.ui.client.CompetitorColorProviderImpl;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class RaceMapHelper {
    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);
    private static Timer raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
    private static RaceTimesInfoProvider raceTimesInfoProvider;
    private static RaceMap raceboardPerspective;

    public static class RVWrapper {
        public RVWrapper(RaceMap raceboardPerspective2, CompetitorSelectionModel competitorSelectionProvider) {
            this.raceboardPerspective = raceboardPerspective2;
            this.csel = competitorSelectionProvider;
        }

        public RaceMap raceboardPerspective;
        public CompetitorSelectionModel csel;
    }

    public static void create(SailingServiceAsync sailingService, ErrorReporter errorReporter, String leaderBoardName,
            UUID eventId, EventDTO event, EventBus eventBus,
            SailingDispatchSystem sailingDispatchSystem, AsyncCallback<RVWrapper> callback) {

        raceboardTimer.reset();
        raceboardTimer.setLivePlayDelayInMillis(1000);
        raceboardTimer.setRefreshInterval(1000);

        if (raceTimesInfoProvider == null) {
            raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, HelperSixty.asyncActionsExecutor,
                    errorReporter,
                    new ArrayList<RegattaAndRaceIdentifier>(), 10000l);
        }
        raceTimesInfoProvider.reset();

        StrippedLeaderboardDTO selectedLeaderboard = HelperSixty.getSelectedLeaderboard(event, leaderBoardName);

        sailingService.getCompetitorsOfLeaderboard(leaderBoardName, new AsyncCallback<Iterable<CompetitorDTO>>() {

            @Override
            public void onSuccess(Iterable<CompetitorDTO> competitors) {
                loadRaceTimes(selectedLeaderboard, new RaceTimesInfoProviderListener() {

                    @Override
                    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                            long clientTimeWhenResponseWasReceived) {

                        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                                clientTimeWhenResponseWasReceived);
                        raceboardTimer.play();
                        RegattaAndRaceIdentifier lifeRace = HelperSixty.checkForLiveRace(selectedLeaderboard,
                                serverTimeDuringRequest, raceTimesInfoProvider);

                        if (lifeRace != null) {
                            sailingService.getCompetitorBoats(lifeRace,
                                    new AsyncCallback<Map<CompetitorDTO, BoatDTO>>() {
                                        @Override
                                        public void onSuccess(Map<CompetitorDTO, BoatDTO> result) {
                                            createRaceMapIfNotExist(lifeRace, selectedLeaderboard, result, competitors,
                                                    sailingService, HelperSixty.asyncActionsExecutor, errorReporter,
                                                    raceboardTimer,
                                                    callback, clientTimeWhenResponseWasReceived,
                                                    serverTimeDuringRequest, clientTimeWhenRequestWasSent,
                                                    raceTimesInfo);
                                            eventBus.fireEvent(
                                                    new SlideHeaderEvent("Currently Live", lifeRace.getRaceName()));
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            callback.onFailure(
                                                    new IllegalStateException("Error getting Competitor Boats"));
                                        }
                                    });
                        } else {
                            callback.onFailure(new IllegalStateException("No Life Race Found"));
                        }
                    }
                }, raceTimesInfoProvider);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(new IllegalStateException("Error getting Competitors"));
            }
        });
    }

    protected static void loadRaceTimes(AbstractLeaderboardDTO selectedLeaderboard,
            RaceTimesInfoProviderListener callback, RaceTimesInfoProvider raceTimesInfoProvider) {
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
            private boolean alreadyfired;

            @Override
            public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                    long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                    long clientTimeWhenResponseWasReceived) {
                if (alreadyfired) {
                    return;
                }
                alreadyfired = true;
                callback.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                        clientTimeWhenResponseWasReceived);
                raceTimesInfoProvider.removeRaceTimesInfoProviderListener(this);
            }
        });

    }

    private static void createRaceMapIfNotExist(RegattaAndRaceIdentifier currentLiveRace,
            StrippedLeaderboardDTO selectedLeaderboard, Map<CompetitorDTO, BoatDTO> result,
            Iterable<CompetitorDTO> competitors, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, Timer raceboardTimer,
            AsyncCallback<RVWrapper> callback,
            long clientTimeWhenResponseWasReceived, Date serverTimeDuringRequest, long clientTimeWhenRequestWasSent,
            Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {

        ArrayList<ZoomTypes> typesToConsiderOnZoom = new ArrayList<>();
        // Other zoom types such as BOATS, TAILS or WINDSENSORS are not currently used as default zoom types.
        typesToConsiderOnZoom.add(ZoomTypes.BUOYS);
        typesToConsiderOnZoom.add(ZoomTypes.BOATS);
        RaceMapZoomSettings autoFollowRace = new RaceMapZoomSettings(typesToConsiderOnZoom, true);

        RaceMapSettings settings = new RaceMapSettings(autoFollowRace, new RaceMapHelpLinesSettings(), false, 15,
                100000l, false, RaceMapSettings.DEFAULT_BUOY_ZONE_RADIUS, false, true, false, false, false, false,
                RaceMapSettings.getDefaultManeuvers(), false);

        RaceMapLifecycle raceMapLifecycle = new RaceMapLifecycle(StringMessages.INSTANCE);

        final CompetitorColorProvider colorProvider = new CompetitorColorProviderImpl(currentLiveRace, result);
        CompetitorSelectionModel competitorSelectionProvider = new CompetitorSelectionModel(
                /* hasMultiSelection */ true, colorProvider);
        competitorSelectionProvider.setCompetitors(competitors);
        raceboardPerspective = new RaceMap(raceMapLifecycle, settings, sailingService, asyncActionsExecutor,
                errorReporter, raceboardTimer, competitorSelectionProvider, StringMessages.INSTANCE, currentLiveRace,
                raceMapResources, false);
        raceboardPerspective.raceTimesInfosReceived(raceTimesInfos, clientTimeWhenRequestWasSent,
                serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        // raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceboardPerspective);
        raceboardTimer.setPlayMode(PlayModes.Live);
        // wait for one update
        raceboardPerspective.onResize();
        callback.onSuccess(new RVWrapper(raceboardPerspective, competitorSelectionProvider));
    }
}
