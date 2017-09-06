package com.sap.sailing.gwt.autoplay.client.utils;

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
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.CompetitorColorProvider;
import com.sap.sailing.gwt.ui.client.CompetitorColorProviderImpl;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.DefaultQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.raceboard.AbstractQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class AutoplayHelper {
    private static final long PRE_RACE_DELAY = 180000;
    private static final long WAIT_TIME_AFTER_END_OF_RACE_MIILIS = 60 * 1000; // 1 min

    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);
    private static RaceTimesInfoProvider raceTimesInfoProvider;
    private static Timer raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
    private static Date startOfLifeRace;
    public static final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
    /**
     * If a racestart is longer ago, the race is never considered live, even if all other checks pass
     */
    private static final long NEGATIVE_SANITY_CHECK = -24 * 60 * 60 * 1000;

    public static long durationOfCurrentLiveRaceRunning() {
        if (startOfLifeRace != null) {
            return raceboardTimer.getLiveTimePointInMillis() - startOfLifeRace.getTime();
        } else {
            return 0;
        }
    }

    public static void getLiveRace(SailingServiceAsync sailingService, ErrorReporter errorReporter, EventDTO event,
            String leaderBoardName, SailingDispatchSystem dispatch,
            AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>> callback) {
        raceboardTimer.setLivePlayDelayInMillis(5000);
        raceboardTimer.setRefreshInterval(1000);

        if (raceTimesInfoProvider == null) {
            raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, errorReporter,
                    new ArrayList<RegattaAndRaceIdentifier>(), 10000l);
        }
        raceTimesInfoProvider.reset();

        StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard(event, leaderBoardName);
        for (RaceColumnDTO race : selectedLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                    raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                }
            }
        }
        if (raceTimesInfoProvider.getRaceIdentifiers().isEmpty()) {
            throw new IllegalStateException(
                    "No raceidentifier was found, cannot determine currently LifeRace, check event configuration");
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
                raceTimesInfoProvider.removeRaceTimesInfoProviderListener(this);
                raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                        clientTimeWhenResponseWasReceived);
                Pair<Long, RegattaAndRaceIdentifier> timeToStartAndRaceIdentifier = checkForLiveRace(
                        selectedLeaderboard, serverTimeDuringRequest, raceTimesInfoProvider);
                callback.onSuccess(timeToStartAndRaceIdentifier);
            }
        });

    }

    /**
     * functional sideeffect free method for getting a leaderboard from an event based on the name
     */
    public static StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event, String leaderBoardName) {
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.name.equals(leaderBoardName)) {
                    return leaderboard;
                }
            }
        }
        return null;
    }

    /**
     * Side effect free method to get a LifeRace from a timesProvider and a leaderboard
     */
    public static Pair<Long, RegattaAndRaceIdentifier> checkForLiveRace(AbstractLeaderboardDTO currentLeaderboard,
            Date serverTimeDuringRequest, RaceTimesInfoProvider raceTimesInfoProvider) {
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceColumnDTO race : currentLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.getStartOfRace() != null
                            && (raceTimes.endOfRace == null || serverTimeDuringRequest
                                    .getTime() < raceTimes.endOfRace.getTime() + WAIT_TIME_AFTER_END_OF_RACE_MIILIS)) {
                        long startTimeInMs = raceTimes.getStartOfRace().getTime();
                        long delayToLiveInMs = raceTimes.delayToLiveInMs;
                        long startIn = startTimeInMs - serverTimeDuringRequest.getTime() - delayToLiveInMs;
                        if (startIn <= PRE_RACE_DELAY && startIn > NEGATIVE_SANITY_CHECK) {
                            startOfLifeRace = raceTimes.getStartOfRace();
                            return new Pair<Long, RegattaAndRaceIdentifier>(startIn, raceIdentifier);
                        }
                    }
                }
            }
        }
        startOfLifeRace = null;
        return null;
    }

    public static class RVWrapper {

        public RVWrapper(RaceMap raceboardPerspective2, CompetitorSelectionModel competitorSelectionProvider) {
            this.raceboardPerspective = raceboardPerspective2;
            this.csel = competitorSelectionProvider;
        }

        public RaceMap raceboardPerspective;
        public CompetitorSelectionModel csel;
    }

    public static void create(SailingServiceAsync sailingService, ErrorReporter errorReporter, String leaderBoardName,
            UUID eventId, EventDTO event, EventBus eventBus, SailingDispatchSystem sailingDispatchSystem,
            RegattaAndRaceIdentifier regattaAndRaceIdentifier, AsyncCallback<RVWrapper> callback) {
        GWT.log("Creating map for " + regattaAndRaceIdentifier);
        raceboardTimer.setLivePlayDelayInMillis(5000);
        raceboardTimer.setRefreshInterval(1000);

        if (raceTimesInfoProvider == null) {
            raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, AutoplayHelper.asyncActionsExecutor,
                    errorReporter, new ArrayList<RegattaAndRaceIdentifier>(), 10000l);
        }
        raceTimesInfoProvider.reset();

        StrippedLeaderboardDTO selectedLeaderboard = AutoplayHelper.getSelectedLeaderboard(event, leaderBoardName);

        sailingService.getCompetitorsOfLeaderboard(leaderBoardName, new AsyncCallback<Iterable<CompetitorDTO>>() {

            @Override
            public void onSuccess(Iterable<CompetitorDTO> competitors) {
                loadRaceTimes(selectedLeaderboard, new RaceTimesInfoProviderListener() {
                   boolean alreadyFired;
                    @Override
                    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                            long clientTimeWhenResponseWasReceived) {

                        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                                clientTimeWhenResponseWasReceived);
                        raceboardTimer.play();

                        if (regattaAndRaceIdentifier != null && !alreadyFired) {
                            alreadyFired = true;
                            sailingService.getCompetitorBoats(regattaAndRaceIdentifier,
                                    new AsyncCallback<Map<CompetitorDTO, BoatDTO>>() {
                                        @Override
                                        public void onSuccess(Map<CompetitorDTO, BoatDTO> result) {
                                            createRaceMapIfNotExist(regattaAndRaceIdentifier, selectedLeaderboard,
                                                    result, competitors, sailingService,
                                                    AutoplayHelper.asyncActionsExecutor, errorReporter, raceboardTimer,
                                                    callback, clientTimeWhenResponseWasReceived,
                                                    serverTimeDuringRequest, clientTimeWhenRequestWasSent,
                                                    raceTimesInfo, new DefaultQuickRanksDTOProvider());
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            callback.onFailure(
                                                    new IllegalStateException("Error getting Competitor Boats"));
                                        }
                                    });
                        } else {
                            callback.onFailure(new IllegalStateException("No Live Race Found"));
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
            AsyncCallback<RVWrapper> callback, long clientTimeWhenResponseWasReceived, Date serverTimeDuringRequest,
            long clientTimeWhenRequestWasSent, Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos,
            AbstractQuickRanksDTOProvider provider) {

        ArrayList<ZoomTypes> typesToConsiderOnZoom = new ArrayList<>();
        // Other zoom types such as BOATS, TAILS or WINDSENSORS are not currently used as default zoom types.
        typesToConsiderOnZoom.add(ZoomTypes.BUOYS);
        typesToConsiderOnZoom.add(ZoomTypes.BOATS);
        RaceMapZoomSettings autoFollowRace = new RaceMapZoomSettings(typesToConsiderOnZoom, true);

        RaceMapSettings settings = new RaceMapSettings(autoFollowRace, new RaceMapHelpLinesSettings(), false, 15,
                100000l, false, RaceMapSettings.DEFAULT_BUOY_ZONE_RADIUS, false, true, false, false, false, false,
                RaceMapSettings.getDefaultManeuvers(), false, false);

        RaceMapLifecycle raceMapLifecycle = new RaceMapLifecycle(StringMessages.INSTANCE);

        final CompetitorColorProvider colorProvider = new CompetitorColorProviderImpl(currentLiveRace, result);
        CompetitorSelectionModel competitorSelectionProvider = new CompetitorSelectionModel(
                /* hasMultiSelection */ true, colorProvider);
        competitorSelectionProvider.setCompetitors(competitors);
        RaceMap raceboardPerspective = new RaceMap(null, null, raceMapLifecycle, settings, sailingService,
                asyncActionsExecutor, errorReporter, raceboardTimer, competitorSelectionProvider,
                new RaceCompetitorSet(competitorSelectionProvider), StringMessages.INSTANCE, currentLiveRace,
                raceMapResources, false, provider);
        raceboardPerspective.raceTimesInfosReceived(raceTimesInfos, clientTimeWhenRequestWasSent,
                serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        raceboardTimer.setPlayMode(PlayModes.Live);
        // wait for one update
        raceboardPerspective.onResize();
        callback.onSuccess(new RVWrapper(raceboardPerspective, competitorSelectionProvider));
    }

}
