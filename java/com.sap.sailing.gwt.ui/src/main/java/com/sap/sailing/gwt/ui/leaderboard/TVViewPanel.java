package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentChecker.UserAgentTypes;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewModes;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public class TVViewPanel extends SimplePanel implements RaceTimesInfoProviderListener {
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentTypes userAgentType;
    private final UserDTO userDTO;
    private final LogoAndTitlePanel logoAndTitlePanel;
    private final DockLayoutPanel dockPanel;
    
    private final Timer timer;
    private RaceTimesInfoProvider raceTimesInfoProvider;
    
    private LeaderboardPanel leaderboardPanel;
    private LeaderboardDTO leaderboard;
    private boolean leaderboardIsWiget;
    
    private Label raceBoardHeader;
    private RaceBoardPanel raceBoardPanel;
    private FlowPanel timePanel;
    private RaceIdentifier currentRace;
    private boolean raceBoardIsWidget;
    
    private boolean debugMode = true; //TODO delete after testing
    
    public TVViewPanel(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            String leaderboardName, UserAgentTypes userAgentType, UserDTO userDTO, Timer timer,
            LogoAndTitlePanel logoAndTitlePanel, DockLayoutPanel dockPanel) {
        setSize("100%", "100%");
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgentType = userAgentType;
        this.userDTO = userDTO;
        this.logoAndTitlePanel = logoAndTitlePanel;
        this.dockPanel = dockPanel;
        this.timer = timer;
        leaderboardIsWiget = false;
        raceBoardIsWidget = false;
        raceBoardPanel = null;
        timePanel = null;
        
        timer.play();
        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, errorReporter, new ArrayList<RaceIdentifier>(), 1000l);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
        
        leaderboardPanel = createLeaderboardPanel(leaderboardName);
        leaderboard = null;
        showLeaderboard();
    }
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName) {
        LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, /* autoExpandFirstRace */ false); 
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        Timer timer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
        timer.play();
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, settings,
        /* preSelectedRace */null, selectionModel, timer, leaderboardName, null, errorReporter, stringMessages,
                userAgentType) {
            @Override
            protected void setLeaderboard(LeaderboardDTO leaderboard) {
                super.setLeaderboard(leaderboard);
                TVViewPanel.this.leaderboard = leaderboard;
                createOrUpdateRaceTimesInfoProvider();
            }
        };
        return leaderboardPanel;
    }
    
    private void createOrUpdateRaceTimesInfoProvider() {
        boolean providerChanged = false;
        for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
            RaceIdentifier raceIdentifier = race.getRaceIdentifier();
            if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                providerChanged = true;
            }
        }
        if (providerChanged) {
            raceTimesInfoProvider.forceTimesInfosUpdate();
            //TODO delete after testing
            debugMode = true;
            //
        }
    }
    
    private RaceBoardPanel createRaceBoardPanel(String leaderboardName, RaceIdentifier raceToShow) {
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RaceIdentifier> singletonList = Collections.singletonList(raceToShow);
        raceSelectionModel.setSelection(singletonList);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, userDTO, timer, raceSelectionModel, leaderboardName, null,
                errorReporter, stringMessages, userAgentType, RaceBoardViewModes.ONESCREEN, raceTimesInfoProvider);
        return raceBoardPanel;
    }
    
    private void showLeaderboard() {
        if (!leaderboardIsWiget) {
            if (leaderboard != null) {
                //Resetting the settings of the leaderboard panel to prevent, that some race columns get lost
                List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
                for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                    namesOfRaceColumnsToShow.add(race.getRaceColumnName());
                }
                LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                        namesOfRaceColumnsToShow, null, false);
                leaderboardPanel.updateSettings(settings);
            }
            setWidget(leaderboardPanel);
            if (raceBoardPanel != null) {
                logoAndTitlePanel.remove(raceBoardPanel.getNavigationWidget());
                logoAndTitlePanel.remove(raceBoardHeader);
                dockPanel.remove(timePanel);
                raceBoardPanel = null;
            }
            currentRace = null;
            
            leaderboardIsWiget = true;
        }
    }
    
    private void showRaceBoard() {
        if (!raceBoardIsWidget) {
            raceBoardHeader = new Label(currentRace.getRaceName());
            raceBoardHeader.addStyleName("RaceBoardHeader");
            logoAndTitlePanel.add(raceBoardHeader);
            logoAndTitlePanel.add(raceBoardPanel.getNavigationWidget());
            
            timePanel = createTimePanel();
            dockPanel.insertSouth(timePanel, 122, dockPanel.getWidget(0));
            
            //TODO The code below causes exceptions, because the race board isn't completely rendered (RaceMap onResize() fails)
            //        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            //            @Override
            //            public void execute() {
            //                raceBoardPanel.setWindChartVisible(true);
            //            }
            //        });
            setWidget(raceBoardPanel);
            //Setting the size or the race board wouldn't be displayed
            raceBoardPanel.setSize("100%", "100%");
            
            raceBoardIsWidget = true;
        }
    }

    private FlowPanel createTimePanel() {
        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimeWidget());
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        if (currentRace == null) {
            currentRace = getFirstStartedAndUnfinishedRace();
            if (currentRace != null) {
                raceBoardPanel = createRaceBoardPanel(leaderboard.name, currentRace);
                showRaceBoard();
                //TODO delete after testing
                final RaceTimesInfoDTO currentRaceTimes = raceTimesInfo.get(currentRace);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        timer.setTime(currentRaceTimes.getStartOfRace().getTime());
                        debugMode = false;
                    }
                });
                //
            } else {
                showLeaderboard();
            }
        } else {
            RaceTimesInfoDTO currentRaceTimes = raceTimesInfo.get(currentRace);
            //TODO add check for live mode
            if (currentRaceTimes.endOfRace != null && timer.getTime().after(currentRaceTimes.endOfRace)) {
                showLeaderboard();
            }
        }
    }
    
    private RaceIdentifier getFirstStartedAndUnfinishedRace() {
        RaceIdentifier firstStartedAndUnfinishedRace = null;
        Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
            RaceIdentifier raceIdentifier = race.getRaceIdentifier();
            RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
            if (raceIdentifier != null && raceTimes != null && raceTimes.startOfTracking != null
                    /*&& raceTimes.endOfRace == null*/ && debugMode ) { //TODO reset after testing
                firstStartedAndUnfinishedRace = raceIdentifier;
                break;
            }
        }
        return firstStartedAndUnfinishedRace;
    }

}
