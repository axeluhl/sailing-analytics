package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.common.client.CSS3Util;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

public class AutoPlayController implements RaceTimesInfoProviderListener {
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

    // leaderboard related attributes
    private LeaderboardDTO leaderboard;
   
    // raceboard related attributes
    private RegattaAndRaceIdentifier currentLiveRace;
    private boolean showRaceDetails;
    private final ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings; 
    private final ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings;
    private final PerspectiveLifecycleAndComponentSettings leaderboardPerspectiveComponentLifecyclesAndSettings;
    private final PerspectiveLifecycleAndComponentSettings raceboardPerspectiveComponentLifecyclesAndSettings;
    private final LeaderboardPerspectiveSettings leaderboardPerspectiveSettings;
    private final RaceBoardPerspectiveSettings raceboardPerspectiveSettings;
    private final PlayerView playerView;
    private final AutoPlayerConfiguration autoPlayerConfiguration;
    private Widget currentContentWidget;

    private static int SAP_HEADER_HEIGHT = 70;
    
    public AutoPlayController(SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, ErrorReporter errorReporter, AutoPlayerConfiguration autoPlayerConfiguration,
            UserAgentDetails userAgent, long delayToLiveInMillis, boolean showRaceDetails, PlayerView playerView,
            ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings, 
            ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings,
            PerspectiveLifecycleAndComponentSettings leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings raceboardPerspectiveComponentLifecyclesAndSettings) {
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.autoPlayerConfiguration = autoPlayerConfiguration;
        this.userAgent = userAgent;
        this.showRaceDetails = showRaceDetails;
        this.playerView = playerView;
        this.leaderboardPerspectiveLifecycleAndSettings = leaderboardPerspectiveLifecycleAndSettings;
        this.raceboardPerspectiveLifecycleAndSettings = raceboardPerspectiveLifecycleAndSettings;
        this.leaderboardPerspectiveComponentLifecyclesAndSettings = leaderboardPerspectiveComponentLifecyclesAndSettings;
        this.raceboardPerspectiveComponentLifecyclesAndSettings = raceboardPerspectiveComponentLifecyclesAndSettings;
        this.leaderboardPerspectiveSettings =leaderboardPerspectiveLifecycleAndSettings.getSettings();
        this.raceboardPerspectiveSettings = raceboardPerspectiveLifecycleAndSettings.getSettings();

        asyncActionsExecutor = new AsyncActionsExecutor();
        leaderboard = null;
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
        
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if(AutoPlayController.this.leaderboardPerspectiveSettings.isLeaderboardAutoZoom()) {
                    autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
                }
            }
        });
    }
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, LeaderboardSettings leaderboardSettings, boolean showRaceDetails) {
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, true,
                /* preSelectedRace */null, selectionModel, leaderboardTimer, /*leaderboardGroupName*/ "", leaderboardName,
                errorReporter, StringMessages.INSTANCE, userAgent, showRaceDetails, /* competitorSearchTextBox */ null, /* showRegattaRank */
                /* showSelectionCheckbox */false, /* raceTimesInfoProvider */null, false, /* autoExpandLastRaceColumn */
                /* adjustTimerDelay */true, /*autoApplyTopNFilter*/ false, false) {
            @Override
            protected void setLeaderboard(LeaderboardDTO leaderboard) {
                super.setLeaderboard(leaderboard);
                AutoPlayController.this.leaderboard = leaderboard;
                updateRaceTimesInfoProvider();
            }
        };
        leaderboardPanel.getContentWidget().getElement().getStyle().setFontWeight(FontWeight.BOLD);
        return leaderboardPanel;
    }
    
    private void updateRaceTimesInfoProvider() {
        boolean addedRaces = false;
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
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
    
    private RaceBoardPanel createRaceBoardPanel(String leaderboardName, RegattaAndRaceIdentifier raceToShow, Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats) {
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, mediaService, userService, asyncActionsExecutor,
                competitorsAndTheirBoats, raceboardTimer, raceToShow, leaderboardName, null, /* event */null, raceboardPerspectiveSettings,
                errorReporter, StringMessages.INSTANCE, userAgent, raceTimesInfoProvider, /* showMapControls */ false,
                /* isScreenLargeEnoughToOfferChartSupport */ true);
        return raceBoardPanel;
    }
    
    private void showLeaderboard() {
        if (activeTvView != AutoPlayModes.Leaderboard) {
            playerView.clear();
            
            boolean withFullscreenButton = autoPlayerConfiguration.isFullscreenMode() && isInitialScreen;
            String headerTitle = TextMessages.INSTANCE.leaderboard() +  ": " + autoPlayerConfiguration.getLeaderboardName();
            
            SAPHeader sapHeader = new SAPHeader(headerTitle, withFullscreenButton);
            playerView.getDockPanel().addNorth(sapHeader, SAP_HEADER_HEIGHT);
            
            LeaderboardSettings leaderboardSettings = null; 
        
            SAPHeaderLifecycle.SAPHeaderConstructionParameters sapHeaderConstParams = createSAPHeaderConstructionParameters(headerTitle, withFullscreenButton);
            LeaderboardPanelLifecycle.LeaderboardPanelConstructionParameters leaderboardConstParams = createLeaderboardPanelConstructionParameters(leaderboardSettings, autoPlayerConfiguration.getLeaderboardName(), showRaceDetails);

            LeaderboardWithHeaderPerspectiveLifecycle.LeaderboardWithHeaderPerspectiveConstructorArgs perspectiveArgs = new LeaderboardWithHeaderPerspectiveLifecycle.LeaderboardWithHeaderPerspectiveConstructorArgs(sapHeaderConstParams, leaderboardConstParams);
            LeaderboardWithHeaderPerspectiveLifecycle leaderboardPerspectiveLifeycle = (LeaderboardWithHeaderPerspectiveLifecycle) leaderboardPerspectiveLifecycleAndSettings.getComponentLifecycle();
            leaderboardPerspectiveLifeycle.createComponent(perspectiveArgs, leaderboardPerspectiveLifecycleAndSettings.getSettings());

            LeaderboardPanel leaderboardPanel = createLeaderboardPanel(autoPlayerConfiguration.getLeaderboardName(), leaderboardSettings, showRaceDetails);
            OldLeaderboard oldLeaderboard = new OldLeaderboard(leaderboardPanel);
            leaderboardPanel.addLeaderboardUpdateListener(oldLeaderboard);
            
            currentContentWidget = oldLeaderboard.getContentWidget();
            
            if(leaderboardPerspectiveSettings.isLeaderboardAutoZoom()) {
                autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
            } else {
                Double zoom = leaderboardPerspectiveSettings.getLeaderboardZoomFactor();
                zoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget, zoom);
            }

            playerView.getDockPanel().add(oldLeaderboard);
            currentLiveRace = null;
            activeTvView = AutoPlayModes.Leaderboard;
            raceboardTimer.pause();
            leaderboardTimer.setPlayMode(PlayModes.Live);
            
            isInitialScreen = false;
        }
    }

    private SAPHeaderLifecycle.SAPHeaderConstructionParameters createSAPHeaderConstructionParameters(String headerTitle, 
            boolean withFullscreenButton) {
        LeaderboardWithHeaderPerspectiveLifecycle perspectiveLifeycle = (LeaderboardWithHeaderPerspectiveLifecycle) leaderboardPerspectiveLifecycleAndSettings.getComponentLifecycle();
        
        return new SAPHeaderLifecycle.SAPHeaderConstructionParameters(perspectiveLifeycle.getSapHeaderLifecycle(), 
                new SAPHeaderLifecycle.SAPHeaderConstructorArgs(headerTitle, withFullscreenButton), null);
    }

    private LeaderboardPanelLifecycle.LeaderboardPanelConstructionParameters createLeaderboardPanelConstructionParameters(LeaderboardSettings leaderboardSettings,
            String leaderboardName, boolean showRaceDetails) {
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        LeaderboardWithHeaderPerspectiveLifecycle perspectiveLifeycle = (LeaderboardWithHeaderPerspectiveLifecycle) leaderboardPerspectiveLifecycleAndSettings.getComponentLifecycle();

        LeaderboardPanelLifecycle.LeaderboardPanelConstructorArgsV1 v1 = new LeaderboardPanelLifecycle.LeaderboardPanelConstructorArgsV1(sailingService, asyncActionsExecutor,
                leaderboardSettings, true, /* preSelectedRace */null, selectionModel, leaderboardTimer, /*leaderboardGroupName*/ "",
                leaderboardName, errorReporter, StringMessages.INSTANCE, userAgent, showRaceDetails);
                
        return new LeaderboardPanelLifecycle.LeaderboardPanelConstructionParameters(perspectiveLifeycle.getLeaderboardPanelLifecycle(), 
                v1, null);
    }
    
    
    private void zoomContentWidget(final int headerHeight, final Widget contentWidget, final double scaleFactor) {
        if(contentWidget != null) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    boolean invokeAgain = true;
                    if(currentContentWidget.getOffsetHeight() > 50) {
                        scaleContentWidget(headerHeight, contentWidget, scaleFactor);
                        FullscreenUtil.requestFullscreen();

                        invokeAgain = false;
                    }
                    return invokeAgain;
                }
              }, 1000);
        }
    }

    private void autoZoomContentWidget(final int headerHeight, final Widget contentWidget) {
        if(contentWidget != null) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    boolean invokeAgain = true;
                    if(currentContentWidget.getOffsetHeight() > 50) {
                        scaleContentWidget(headerHeight, contentWidget);
                        invokeAgain = false;
                    }
                    return invokeAgain;
                }
              }, 1000);
        }
    }

    private void scaleContentWidget(int headerHeight, Widget contentWidget, double scaleFactor) {
        int clientWidth = Window.getClientWidth();
        int contentWidth = contentWidget.getOffsetWidth();

        double diffX = clientWidth - contentWidth * scaleFactor;
        
        scaleContentWidget(headerHeight, contentWidget, scaleFactor, diffX);
    }
    
    private void scaleContentWidget(int headerHeight, Widget contentWidget) {
        int clientWidth = Window.getClientWidth();
        int clientHeight = Window.getClientHeight() - headerHeight;

        int contentWidth = contentWidget.getOffsetWidth();
        int contentHeight = contentWidget.getOffsetHeight();
        
        double scaleFactorX = clientWidth / (double) contentWidth;
        double scaleFactorY = clientHeight / (double) contentHeight;
        
        Double scaleFactor = scaleFactorX > scaleFactorY ? scaleFactorY : scaleFactorX;
        if(scaleFactor < 1.0) {
            scaleFactor = 1.0;
        }
        double diffX = clientWidth - contentWidth * scaleFactor;

        scaleContentWidget(headerHeight, contentWidget, scaleFactor, diffX);
    }

    private void scaleContentWidget(int headerHeight, Widget contentWidget, double scaleFactor, double diffX) {
        if(scaleFactor > 0.0) {
            CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transform", "translateX(" + diffX / 2.0 + "px) scale(" + scaleFactor + ")");
            CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transformOrigin", "0 0");
        }
    }

    private void showRaceBoard() {
        if (activeTvView != AutoPlayModes.Raceboard) {
            sailingService.getRaceboardData(currentLiveRace.getRegattaName(), currentLiveRace.getRaceName(),
                    autoPlayerConfiguration.getLeaderboardName(), null, null, new AsyncCallback<RaceboardDataDTO>() {
                @Override
                public void onSuccess(RaceboardDataDTO result) {
                    playerView.clear();
                    RaceBoardPanel raceBoardPanel = createRaceBoardPanel(AutoPlayController.this.autoPlayerConfiguration.getLeaderboardName(),
                            currentLiveRace, result.getCompetitorAndTheirBoats());
                    raceBoardPanel.setSize("100%", "100%");
                    FlowPanel timePanel = createTimePanel(raceBoardPanel);
                    
                    final Button toggleButton = raceBoardPanel.getTimePanel().getAdvancedToggleButton();
                    toggleButton.setVisible(false);
                    playerView.getDockPanel().addSouth(timePanel, 67);                     
                    playerView.getDockPanel().add(raceBoardPanel);
                    activeTvView = AutoPlayModes.Raceboard;
                    leaderboardTimer.pause();
                    raceboardTimer.setPlayMode(PlayModes.Live);
                    
//                    raceBoardPanel.setSettingsOfComponents(raceboardPerspective.getSettingsOfComponents());
                    
                    isInitialScreen = false;
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error while loading data for raceboard: " + caught.getMessage());
                }
            });
            
        }
    }

    private FlowPanel createTimePanel(RaceBoardPanel raceBoardPanel) {
        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimePanel());
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
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
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
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
}
