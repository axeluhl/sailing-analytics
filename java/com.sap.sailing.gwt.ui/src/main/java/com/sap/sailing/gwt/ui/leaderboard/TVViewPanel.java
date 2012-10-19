package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
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
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewModes;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class TVViewPanel extends SimplePanel implements RaceTimesInfoProviderListener {
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
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
    private RegattaAndRaceIdentifier currentRace;
    private boolean raceBoardIsWidget;
    
    //private enum TvViews { Leaderboard, Raceboard };
    //private TvViews activeTvView;
    
    /**
     * @param logoAndTitlePanel allowed to be <code>null</code>
     */
    public TVViewPanel(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            String leaderboardName, UserAgentDetails userAgent, Timer timer,
            LogoAndTitlePanel logoAndTitlePanel, DockLayoutPanel dockPanel, boolean showRaceDetails) {
        setSize("100%", "100%");
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.logoAndTitlePanel = logoAndTitlePanel;
        this.dockPanel = dockPanel;
        this.timer = timer;
        leaderboardIsWiget = false;
        raceBoardIsWidget = false;
        raceBoardPanel = null;
        timePanel = null;
        leaderboard = null;
        
        timer.play();
        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, errorReporter, new ArrayList<RegattaAndRaceIdentifier>(), 5000l);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
        leaderboardPanel = createLeaderboardPanel(leaderboardName, showRaceDetails);
        showLeaderboard();
    }
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, boolean showRaceDetails) {
        LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false); 
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        Timer timer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, new AsyncActionsExecutor(), settings,
        /* preSelectedRace */null, selectionModel, timer, leaderboardName, null, errorReporter, stringMessages,
                userAgent, showRaceDetails) {
            @Override
            protected void setLeaderboard(LeaderboardDTO leaderboard) {
                super.setLeaderboard(leaderboard);
                TVViewPanel.this.leaderboard = leaderboard;
                updateRaceTimesInfoProvider();
            }
        };
        return leaderboardPanel;
    }
    
    private void updateRaceTimesInfoProvider() {
        boolean providerChanged = false;
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                    raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                    providerChanged = true;
                }
            }
        }
        if (providerChanged) {
            raceTimesInfoProvider.forceTimesInfosUpdate();
        }
    }
    
    private RaceBoardPanel createRaceBoardPanel(String leaderboardName, RegattaAndRaceIdentifier raceToShow) {
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RegattaAndRaceIdentifier> singletonList = Collections.singletonList(raceToShow);
        raceSelectionModel.setSelection(singletonList);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, null, timer, raceSelectionModel, leaderboardName, null,
                errorReporter, stringMessages, userAgent, RaceBoardViewModes.ONESCREEN, raceTimesInfoProvider);
        return raceBoardPanel;
    }
    
    private void showLeaderboard() {
        if (!leaderboardIsWiget) {
            if (leaderboard != null) {
                //Resetting the settings of the leaderboard panel to prevent, that some race columns get lost
                List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
                for (RaceColumnDTO race : leaderboard.getRaceList()) {
                    namesOfRaceColumnsToShow.add(race.getRaceColumnName());
                }
                LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                        namesOfRaceColumnsToShow, null, null, false);
                leaderboardPanel.updateSettings(settings);
            }
            
            setWidget(leaderboardPanel);
            if (raceBoardPanel != null) {
                if (logoAndTitlePanel != null) {
                    logoAndTitlePanel.remove(raceBoardPanel.getNavigationWidget());
                    logoAndTitlePanel.remove(raceBoardHeader);
                }
                dockPanel.remove(timePanel);
                raceBoardPanel = null;
            }
            currentRace = null;
            
            leaderboardIsWiget = true;
            raceBoardIsWidget = false;
        }
    }
    
    private void showRaceBoard() {
        if (!raceBoardIsWidget) {
            if (logoAndTitlePanel != null) {
                logoAndTitlePanel.add(raceBoardPanel.getNavigationWidget());
            }
            raceBoardHeader = new Label(currentRace.getRaceName());
            raceBoardHeader.addStyleName("RaceBoardHeader");
            if (logoAndTitlePanel != null) {
                logoAndTitlePanel.add(raceBoardHeader);
            }
            
            timePanel = createTimePanel();
            dockPanel.insertSouth(timePanel, 122, dockPanel.getWidget(0));
            
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    // Calling the setVisible method causes exceptions, if the race board isn't fully rendered, so
                    // repeating this command until no exception was thrown
                    try {
                        raceBoardPanel.setWindChartVisible(true);
                    } catch (Throwable e) {
                        return true;
                    }
                    return false;
                }
            }, 1000);
            setWidget(raceBoardPanel);
            //Setting the size or the race board wouldn't be displayed
            raceBoardPanel.setSize("100%", "100%");
            
            raceBoardIsWidget = true;
            leaderboardIsWiget = false;
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
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        if (currentRace == null) {
            currentRace = getFirstStartedAndUnfinishedRace();
            if (currentRace != null) {
                raceBoardPanel = createRaceBoardPanel(leaderboard.name, currentRace);
                showRaceBoard();
            } else {
                showLeaderboard();
            }
        } else {
            RaceTimesInfoDTO currentRaceTimes = raceTimesInfo.get(currentRace);
            //TODO add check for live mode
            if (currentRaceTimes.endOfRace != null && timer.getTime().after(currentRaceTimes.endOfRace)
                    && timer.getPlayMode() == PlayModes.Live) {
                showLeaderboard();
            }
        }
    }
    
    private RegattaAndRaceIdentifier getFirstStartedAndUnfinishedRace() {
        RegattaAndRaceIdentifier firstStartedAndUnfinishedRace = null;
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.endOfRace == null) {
                        firstStartedAndUnfinishedRace = raceIdentifier;
                        break;
                    }
                }
            }
        }
        return firstStartedAndUnfinishedRace;
    }

}
