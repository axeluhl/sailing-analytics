package com.sap.sailing.gwt.ui.raceboard;

import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveConstructorArgs;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

public class RaceBoardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<RaceBoardPerspective, 
    RaceBoardPerspectiveSettings, RaceBoardPerspectiveSettingsDialogComponent,
    RaceBoardPerspectiveLifecycle.ConstructorArgs> {

    private final StringMessages stringMessages;
    private final RaceMapLifecycle raceMapLifecycle;
    private final WindChartLifecycle windChartLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final MultiCompetitorRaceChartLifecycle multiCompetitorRaceChartLifecycle;
    private final MediaPlayerLifecycle mediaPlayerLifecycle;

    public RaceBoardPerspectiveLifecycle(StringMessages stringMessages) {
        this(null, stringMessages);
    }
    
    public RaceBoardPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;

        raceMapLifecycle = new RaceMapLifecycle(stringMessages);
        windChartLifecycle = new WindChartLifecycle(stringMessages);
        leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        multiCompetitorRaceChartLifecycle = new MultiCompetitorRaceChartLifecycle(stringMessages, false);
        mediaPlayerLifecycle = new MediaPlayerLifecycle(stringMessages);
        
        componentLifecycles.add(raceMapLifecycle);
        componentLifecycles.add(windChartLifecycle);
        componentLifecycles.add(leaderboardPanelLifecycle);
        componentLifecycles.add(multiCompetitorRaceChartLifecycle);
        componentLifecycles.add(mediaPlayerLifecycle);
    }
    
    @Override
    public RaceBoardPerspectiveSettingsDialogComponent getSettingsDialogComponent(RaceBoardPerspectiveSettings settings) {
        return new RaceBoardPerspectiveSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public RaceBoardPerspectiveSettings createDefaultSettings() {
        return new RaceBoardPerspectiveSettings();
    }

    @Override
    public RaceBoardPerspectiveSettings cloneSettings(RaceBoardPerspectiveSettings settings) {
        return new RaceBoardPerspectiveSettings(settings.getActiveCompetitorsFilterSetName(), settings.isShowLeaderboard(),
                settings.isShowWindChart(), settings.isShowCompetitorsChart(), settings.isSimulationEnabled(), settings.isCanReplayDuringLiveRaces(),
                settings.isChartSupportEnabled());
    }

    @Override
    public String getLocalizedShortName() {
        return "Race Viewer";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    public RaceMapLifecycle getRaceMapLifecycle() {
        return raceMapLifecycle;
    }

    public WindChartLifecycle getWindChartLifecycle() {
        return windChartLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    public MultiCompetitorRaceChartLifecycle getMultiCompetitorRaceChartLifecycle() {
        return multiCompetitorRaceChartLifecycle;
    }

    public MediaPlayerLifecycle getMediaPlayerLifecycle() {
        return mediaPlayerLifecycle;
    }
    
    @Override
    public RaceBoardPerspective createComponent(ConstructorArgs raceboardPerspectiveConstructorArgs,
            RaceBoardPerspectiveSettings settings) {
        return raceboardPerspectiveConstructorArgs.createComponent(settings);
    }

    public static class ConstructorArgs implements PerspectiveConstructorArgs<RaceBoardPerspective, RaceBoardPerspectiveSettings> {
        private final PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> componentLifecyclesAndSettings;
        private final SailingServiceAsync sailingService;
        private final MediaServiceAsync mediaService;
        private final UserService userService;
        private final AsyncActionsExecutor asyncActionsExecutor;
        private final Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats;
        private final Timer timer;
        private final RegattaAndRaceIdentifier selectedRaceIdentifier;
        private final String leaderboardName;
        private final String leaderboardGroupName;
        private final UUID eventId;
        private final ErrorReporter errorReporter; 
        private final StringMessages stringMessages;
        private final UserAgentDetails userAgent;
        private final RaceTimesInfoProvider raceTimesInfoProvider;
        
        public ConstructorArgs(PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> componentLifecyclesAndSettings,
                SailingServiceAsync sailingService, MediaServiceAsync mediaService,
                UserService userService, AsyncActionsExecutor asyncActionsExecutor,
                Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats, Timer timer,
                RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName,
                String leaderboardGroupName, UUID eventId,
                ErrorReporter errorReporter, StringMessages stringMessages,
                UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider) {
            this.componentLifecyclesAndSettings = componentLifecyclesAndSettings;
            this.sailingService = sailingService;
            this.mediaService = mediaService;
            this.userService = userService;
            this.asyncActionsExecutor = asyncActionsExecutor; 
            this.competitorsAndTheirBoats = competitorsAndTheirBoats;
            this.timer = timer;
            this.selectedRaceIdentifier = selectedRaceIdentifier;
            this.leaderboardName = leaderboardName;
            this.leaderboardGroupName = leaderboardGroupName;
            this.eventId = eventId;
            this.errorReporter =  errorReporter;
            this.stringMessages = stringMessages;
            this.userAgent = userAgent;
            this.raceTimesInfoProvider = raceTimesInfoProvider; 
        }
        
        @Override
        public RaceBoardPerspective createComponent(RaceBoardPerspectiveSettings settings) {
            RaceBoardPerspective raceboardPerspective = new RaceBoardPerspective(settings,
                    componentLifecyclesAndSettings, sailingService, mediaService, userService, asyncActionsExecutor,
                    competitorsAndTheirBoats, timer, selectedRaceIdentifier, leaderboardName, leaderboardGroupName, 
                    eventId, errorReporter, stringMessages, userAgent, raceTimesInfoProvider);            
            return raceboardPerspective;
        }
    }
}
