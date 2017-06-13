package com.sap.sailing.gwt.autoplay.client.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
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
    private static final long WAIT_TIME_AFTER_END_OF_RACE_MIILIS = 60 * 1000; // 1 min  
    
    private static RaceTimesInfoProvider raceTimesInfoProvider;
    private static Timer raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
    public static final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

    public static void getLiveRace(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, EventDTO event, String leaderBoardName, SailingDispatchSystem dispatch,
            AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>> callback) {
        raceboardTimer.reset();
        raceboardTimer.setLivePlayDelayInMillis(1000);
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
                        selectedLeaderboard, serverTimeDuringRequest,
                        raceTimesInfoProvider);
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
                            && (raceTimes.endOfRace == null || serverTimeDuringRequest.getTime() < raceTimes.endOfRace.getTime()+WAIT_TIME_AFTER_END_OF_RACE_MIILIS)) {
                        long startTimeInMs = raceTimes.getStartOfRace().getTime();
                        long delayToLiveInMs = raceTimes.delayToLiveInMs;
                        return new Pair<Long, RegattaAndRaceIdentifier>(
                                startTimeInMs - serverTimeDuringRequest.getTime() - delayToLiveInMs, raceIdentifier);
                    }
                }
            }
        }
        return null;
    }
}
