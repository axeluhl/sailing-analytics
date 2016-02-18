package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspective;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

public class AutoPlayController implements RaceTimesInfoProviderListener, LeaderboardUpdateListener {
    private static final int REFRESH_INTERVAL_IN_MILLIS_LEADERBOARD = 10000;
    private static final long REFRESH_INTERVAL_IN_MILLIS_RACEBOARD = 1000;
    private static final long WAIT_TIME_AFTER_END_OF_RACE_MIILIS = 60 * 1000; // 1 min  
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final AsyncActionsExecutor asyncActionsExecutor;

    /**
     * We use two timers: one for the leaderboard, one for the race board. This way, we can stop one while the other continues.
     * This way, the other component stops its auto-updates.
     */
    private final Timer leaderboardTimer;
    private final Timer raceboardTimer;
    private final RaceTimesInfoProvider raceTimesInfoProvider;  
    private AutoPlayModes activeTvView;
    private boolean isInitialScreen = true;

    // leaderboard perspective related attributes
    private LeaderboardDTO currentLeaderboard;
   
    // raceboard perspective related attributes
    private RegattaAndRaceIdentifier currentLiveRace;
    
    private final PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings; 
    private final PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings;
    private final PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings;
    private final PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings;
    private final PlayerView playerView;
    private final AutoPlayerConfiguration autoPlayerConfiguration;
    
    public AutoPlayController(SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, ErrorReporter errorReporter, AutoPlayerConfiguration autoPlayerConfiguration,
            UserAgentDetails userAgent, long delayToLiveInMillis, boolean showRaceDetails, PlayerView playerView,
            PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings, 
            PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings,
            PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings) {
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.autoPlayerConfiguration = autoPlayerConfiguration;
        this.userAgent = userAgent;
        this.playerView = playerView;
        this.leaderboardPerspectiveLifecycleAndSettings = leaderboardPerspectiveLifecycleAndSettings;
        this.raceboardPerspectiveLifecycleAndSettings = raceboardPerspectiveLifecycleAndSettings;
        this.leaderboardPerspectiveComponentLifecyclesAndSettings = leaderboardPerspectiveComponentLifecyclesAndSettings;
        this.raceboardPerspectiveComponentLifecyclesAndSettings = raceboardPerspectiveComponentLifecyclesAndSettings;

        asyncActionsExecutor = new AsyncActionsExecutor();
        currentLeaderboard = null;
        leaderboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
        leaderboardTimer.setLivePlayDelayInMillis(delayToLiveInMillis);
        leaderboardTimer.setRefreshInterval(REFRESH_INTERVAL_IN_MILLIS_LEADERBOARD);
        leaderboardTimer.play();
        raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
        raceboardTimer.setLivePlayDelayInMillis(delayToLiveInMillis);
        raceboardTimer.setRefreshInterval(REFRESH_INTERVAL_IN_MILLIS_RACEBOARD);
        raceboardTimer.play();

        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, errorReporter, new ArrayList<RegattaAndRaceIdentifier>(), 3000l);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
    }
    
    private void showLeaderboard() {
        if (activeTvView != AutoPlayModes.Leaderboard) {
            playerView.clearContent();
            
            boolean withFullscreenButton = autoPlayerConfiguration.isFullscreenMode() && isInitialScreen;

            LeaderboardWithHeaderPerspective leaderboardPerspective = new LeaderboardWithHeaderPerspective(leaderboardPerspectiveLifecycleAndSettings.getSettings(),
                    leaderboardPerspectiveComponentLifecyclesAndSettings, 
                    sailingService, asyncActionsExecutor,
                    new CompetitorSelectionModel(/* hasMultiSelection */ true), leaderboardTimer,
                    autoPlayerConfiguration.getLeaderboardName(), errorReporter, StringMessages.INSTANCE,
                    userAgent, withFullscreenButton);

            playerView.setContent(leaderboardPerspective);
            currentLiveRace = null;
            activeTvView = AutoPlayModes.Leaderboard;
            raceboardTimer.pause();
            leaderboardTimer.setPlayMode(PlayModes.Live);
            
            leaderboardPerspective.addLeaderboardUpdateListener(this);
            
            isInitialScreen = false;
        }
    }

    private void showRaceBoard() {
        if (activeTvView != AutoPlayModes.Raceboard) {
            sailingService.getRaceboardData(currentLiveRace.getRegattaName(), currentLiveRace.getRaceName(),
                    autoPlayerConfiguration.getLeaderboardName(), null, null, new AsyncCallback<RaceboardDataDTO>() {
                @Override
                public void onSuccess(RaceboardDataDTO result) {
                    playerView.clearContent();

                    RaceBoardPerspective raceboardPerspective = new RaceBoardPerspective(raceboardPerspectiveLifecycleAndSettings.getSettings(),
                            raceboardPerspectiveComponentLifecyclesAndSettings, sailingService, mediaService, userService, asyncActionsExecutor,
                            result.getCompetitorAndTheirBoats(), raceboardTimer, currentLiveRace, autoPlayerConfiguration.getLeaderboardName(), 
                            /** leaderboardGroupName */ null, /** eventId */ null, errorReporter,
                            StringMessages.INSTANCE, userAgent, raceTimesInfoProvider);

                    playerView.setContent(raceboardPerspective);

                    activeTvView = AutoPlayModes.Raceboard;
                    leaderboardTimer.pause();
                    raceboardTimer.setPlayMode(PlayModes.Live);
                    
                    isInitialScreen = false;
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error while loading data for raceboard: " + caught.getMessage());
                }
            });
            
        }
    }

    public void updatePlayMode(AutoPlayModes tvView) {
        switch (tvView) {
            case Leaderboard:
                showLeaderboard();
                break;
            case Raceboard:
                showRaceBoard();
                break;
        }
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        leaderboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        if (currentLiveRace != null) {
            RaceTimesInfoDTO currentRaceTimes = raceTimesInfo.get(currentLiveRace);

            Date endOfRace = currentRaceTimes.endOfRace;
            if (endOfRace != null && raceboardTimer.getTime().getTime() > endOfRace.getTime() + WAIT_TIME_AFTER_END_OF_RACE_MIILIS
                && raceboardTimer.getPlayMode() == PlayModes.Live) {
                updatePlayMode(AutoPlayModes.Leaderboard);
            }
        } else {
            currentLiveRace = checkForLiveRace(raceboardTimer.getLiveTimePointInMillis());
            if (currentLiveRace != null) {
                updatePlayMode(AutoPlayModes.Raceboard);
            } else {
                updatePlayMode(AutoPlayModes.Leaderboard);
            }
        }
    }

    private RegattaAndRaceIdentifier checkForLiveRace(long serverTimePointAsMillis) {
        RegattaAndRaceIdentifier result = null;
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceColumnDTO race : currentLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if(raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.getStartOfRace() != null && raceTimes.endOfRace == null) {
                        long startTimeInMs = raceTimes.getStartOfRace().getTime();
                        long timeToSwitchBeforeRaceStartInMs = autoPlayerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds() * 1000;
                        long delayToLiveInMs = raceTimes.delayToLiveInMs;
                        // the switch to the live race should happen at a defined timepoint before the race start (default is 3 min) 
                        if (serverTimePointAsMillis - delayToLiveInMs > startTimeInMs - timeToSwitchBeforeRaceStartInMs) {
                            return raceIdentifier;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private void updateRaceTimesInfoProvider() {
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

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        this.currentLeaderboard = leaderboard;
        updateRaceTimesInfoProvider();
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        // no-op
    }
}
