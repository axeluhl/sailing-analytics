package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
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
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
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

public class RaceMapHelper {
    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);

    public static void create(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, String leaderBoardName, UUID eventId,EventDTO event, EventBus eventBus,SailingDispatchSystem sailingDispatchSystem, AsyncCallback<RaceMap> callback){
        Timer raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
        raceboardTimer.setLivePlayDelayInMillis(1000);
        raceboardTimer.setRefreshInterval(1000);

        raceboardTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                GWT.log("Time change " + newTime);
            }
        });

        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, errorReporter,
                new ArrayList<RegattaAndRaceIdentifier>(), 3000l);

        loadMiniLeaderBoard(new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {

            @Override
            public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> leaderBoard) {
                StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard(event,
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
                                                serverTimeDuringRequest, raceTimesInfoProvider);
                                        if (lifeRace != null) {
                                            sailingService.getCompetitorBoats(lifeRace,
                                                    new AsyncCallback<Map<CompetitorDTO, BoatDTO>>() {
                                                        @Override
                                                        public void onSuccess(Map<CompetitorDTO, BoatDTO> result) {
                                                            createRaceMapIfNotExist(lifeRace, selectedLeaderboard,
                                                                    result, competitors, sailingService, asyncActionsExecutor, errorReporter, raceboardTimer, raceTimesInfoProvider,callback);
                                                            eventBus.fireEvent(new SlideHeaderEvent(
                                                                    "Currently Live", lifeRace.getRaceName()));
                                                        }

                                                        @Override
                                                        public void onFailure(Throwable caught) {
                                                            callback.onFailure(new IllegalStateException("Error getting Competitor Boats"));
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

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(new IllegalStateException("Could not load MiniLeaderboard"));
            }
        },eventId,sailingDispatchSystem,leaderBoardName);
    }

    protected static void loadRaceTimes(AbstractLeaderboardDTO selectedLeaderboard, RaceTimesInfoProviderListener callback, RaceTimesInfoProvider raceTimesInfoProvider) {
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

    private static void loadMiniLeaderBoard(AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>> callback, UUID eventId, SailingDispatchSystem sailingDispatchSystem, String leaderBoardName) {
        GetMiniLeaderbordAction leaderboardAction = new GetMiniLeaderbordAction(eventId, leaderBoardName);
        sailingDispatchSystem.execute(leaderboardAction, callback);
    }

    private static void createRaceMapIfNotExist(RegattaAndRaceIdentifier currentLiveRace,
            StrippedLeaderboardDTO selectedLeaderboard, Map<CompetitorDTO, BoatDTO> result,
            Iterable<CompetitorDTO> competitors, SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, Timer raceboardTimer, RaceTimesInfoProvider raceTimesInfoProvider, AsyncCallback<RaceMap> callback) {

        ArrayList<ZoomTypes> typesToConsiderOnZoom = new ArrayList<>();
        // Other zoom types such as BOATS, TAILS or WINDSENSORS are not currently used as default zoom types.
        typesToConsiderOnZoom.add(ZoomTypes.BUOYS);
        typesToConsiderOnZoom.add(ZoomTypes.BOATS);
        typesToConsiderOnZoom.add(ZoomTypes.TAILS);
        RaceMapZoomSettings autoFollowRace = new RaceMapZoomSettings(typesToConsiderOnZoom, true);

        RaceMapSettings settings = new RaceMapSettings(autoFollowRace, new RaceMapHelpLinesSettings(), false,
                15, 100000l, false, RaceMapSettings.DEFAULT_BUOY_ZONE_RADIUS, false, true, false, false, false, false,
                RaceMapSettings.getDefaultManeuvers(), false);

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
        callback.onSuccess(raceboardPerspective);

    }

    private static RegattaAndRaceIdentifier checkForLiveRace(AbstractLeaderboardDTO currentLeaderboard,
            Date serverTimeDuringRequest, RaceTimesInfoProvider raceTimesInfoProvider) {
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

    private static StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event, String leaderBoardName) {
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
