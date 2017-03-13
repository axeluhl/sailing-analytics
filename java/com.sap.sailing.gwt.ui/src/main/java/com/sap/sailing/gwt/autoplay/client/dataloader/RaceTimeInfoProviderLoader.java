package com.sap.sailing.gwt.autoplay.client.dataloader;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class RaceTimeInfoProviderLoader implements AutoPlayDataLoader<AutoPlayClientFactorySixtyInch> {
    protected static Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo;
    protected static Date serverTimeDuringRequest;
    protected static long clientTimeWhenRequestWasSent;
    protected static long clientTimeWhenResponseWasReceived;
    private RaceTimesInfoProvider raceTimesInfoProvider;
    private AutoPlayClientFactorySixtyInch clientFactory;

    @Override
    public void startLoading(EventBus eventBus, AutoPlayClientFactorySixtyInch clientFactory) {
        this.clientFactory = clientFactory;
        SailingServiceAsync sailingService = clientFactory.getSailingService();
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        ErrorReporter errorReporter = clientFactory.getErrorReporter();
        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, errorReporter,
                new ArrayList<RegattaAndRaceIdentifier>(), 3000l);
        clientFactory.getSlideCtx().setRaceTimesInfoProvider(raceTimesInfoProvider);

        eventBus.addHandler(EventChanged.TYPE, new EventChanged.Handler() {

            @Override
            public void onEventChanged(EventChanged e) {
                checkUpdate();
            }
        });

        eventBus.addHandler(MiniLeaderboardUpdatedEvent.TYPE, new MiniLeaderboardUpdatedEvent.Handler() {

            @Override
            public void handleNoOpEvent(MiniLeaderboardUpdatedEvent e) {
                checkUpdate();
            }
        });

        raceTimesInfoProvider.addRaceTimesInfoProviderListener(new RaceTimesInfoProviderListener() {
            @Override
            public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                    long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                    long clientTimeWhenResponseWasReceived) {
                RaceTimeInfoProviderLoader.raceTimesInfo = raceTimesInfo;
                RaceTimeInfoProviderLoader.serverTimeDuringRequest = serverTimeDuringRequest;
                RaceTimeInfoProviderLoader.clientTimeWhenRequestWasSent = clientTimeWhenRequestWasSent;
                RaceTimeInfoProviderLoader.clientTimeWhenResponseWasReceived = clientTimeWhenResponseWasReceived;
                checkUpdate();
            }
        });
    }

    protected void checkUpdate() {
        GetMiniLeaderboardDTO leaderBoardDTO = clientFactory.getSlideCtx().getMiniLeaderboardDTO();
        EventDTO eventDTO = clientFactory.getSlideCtx().getEvent();
        String leaderBoardName = clientFactory.getSlideCtx().getSettings().getLeaderBoardName();
        
        RegattaAndRaceIdentifier lifeRace = null;
        if (leaderBoardDTO != null && eventDTO != null) {
            StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard(eventDTO, leaderBoardName);
            updateRaceTimesInfoProvider(selectedLeaderboard);
            if (serverTimeDuringRequest != null) {
                lifeRace = checkForLiveRace(selectedLeaderboard);
            }
        }

        
        
        clientFactory.getSlideCtx().updateRaceTimeInfos(raceTimesInfo, clientTimeWhenRequestWasSent,
                serverTimeDuringRequest, clientTimeWhenResponseWasReceived, lifeRace);
    }

    private void updateRaceTimesInfoProvider(AbstractLeaderboardDTO currentLeaderboard) {
        boolean addedRaces = false;
        for (RaceColumnDTO race : currentLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                    raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                    addedRaces = true;
                }
            }
        }
        if (addedRaces) {
            raceTimesInfoProvider.forceTimesInfosUpdate();
        }
    }

    private RegattaAndRaceIdentifier checkForLiveRace(AbstractLeaderboardDTO currentLeaderboard) {
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

    @Override
    public void stopLoading() {
        // TODO Auto-generated method stub

    }

}
