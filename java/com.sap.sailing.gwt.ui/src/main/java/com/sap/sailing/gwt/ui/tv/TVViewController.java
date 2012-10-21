package com.sap.sailing.gwt.ui.tv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
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
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewModes;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class TVViewController implements RaceTimesInfoProviderListener {
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final LogoAndTitlePanel logoAndTitlePanel;
    private final DockLayoutPanel dockPanel;
    
    private final Timer timer;
    private final RaceTimesInfoProvider raceTimesInfoProvider;  
    public LeaderboardDTO leaderboard;
    private RegattaAndRaceIdentifier currentLiveRace;
    
    private final String leaderboardName;
    private boolean showRaceDetails;
    private boolean showWindChart;
    private TVViews activeTvView;
    
    /**
     * @param logoAndTitlePanel allowed to be <code>null</code>
     */
    public TVViewController(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            String leaderboardName, UserAgentDetails userAgent, LogoAndTitlePanel logoAndTitlePanel,
            DockLayoutPanel dockPanel, long delayToLiveMillis, boolean showRaceDetails) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.userAgent = userAgent;
        this.logoAndTitlePanel = logoAndTitlePanel;
        this.dockPanel = dockPanel;
        this.showRaceDetails = showRaceDetails;
        
        showWindChart = false;
        leaderboard = null;
        
        timer = new Timer(PlayModes.Live, 1000l);
        timer.setLivePlayDelayInMillis(delayToLiveMillis);
        timer.play();

        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, errorReporter, new ArrayList<RegattaAndRaceIdentifier>(), 3000l);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
    }
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, boolean showRaceDetails) {
        LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false); 
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        Timer timer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, new AsyncActionsExecutor(), settings,
        /* preSelectedRace */null, selectionModel, timer, leaderboardName, null, errorReporter, stringMessages,
                userAgent, showRaceDetails, new ExplicitRaceColumnSelection()) {
            @Override
            protected void setLeaderboard(LeaderboardDTO leaderboard) {
                super.setLeaderboard(leaderboard);
                TVViewController.this.leaderboard = leaderboard;
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
    
    private void clearContentPanels() {
        int childWidgetCount = dockPanel.getWidgetCount();
        for(int i = 0; i < childWidgetCount; i++) {
            Widget widget = dockPanel.getWidget(i);
            
            // don't remove the logoAndTitlePanel if exist
            if(logoAndTitlePanel == null || widget != logoAndTitlePanel) {
                dockPanel.remove(widget);
            }
        }
    }
    
    private void showLeaderboard() {
        if(activeTvView != TVViews.Leaderboard) {
            clearContentPanels();
            
            LeaderboardPanel leaderboardPanel = createLeaderboardPanel(leaderboardName, showRaceDetails);
            ScrollPanel leaderboardContentPanel = new ScrollPanel();
            leaderboardContentPanel.add(leaderboardPanel);

            if(leaderboard != null) {
                //Resetting the settings of the leaderboard panel to prevent, that some race columns get lost
                List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
                for (RaceColumnDTO race : leaderboard.getRaceList()) {
                    namesOfRaceColumnsToShow.add(race.getRaceColumnName());
                }
                LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                        namesOfRaceColumnsToShow, null, null, false);
                leaderboardPanel.updateSettings(settings);
            }

            dockPanel.add(leaderboardContentPanel);

            if(logoAndTitlePanel != null) {
                logoAndTitlePanel.setTitle(leaderboardName);
            }
            
            currentLiveRace = null;
            activeTvView = TVViews.Leaderboard;
        }
    }
    
    private void showRaceBoard() {
        if(activeTvView != TVViews.Raceboard) {
            clearContentPanels();

            RaceBoardPanel raceBoardPanel = createRaceBoardPanel(leaderboardName, currentLiveRace);
            raceBoardPanel.setSize("100%", "100%");
            if(showWindChart) {
                raceBoardPanel.setWindChartVisible(true);
            }

//            FlowPanel toolbarPanel = new FlowPanel();
//            toolbarPanel.add(raceBoardPanel.getNavigationWidget());
//            dockPanel.addNorth(toolbarPanel, 40);
            
            FlowPanel timePanel = createTimePanel(raceBoardPanel);
            
            dockPanel.addSouth(timePanel, 90);                     
            dockPanel.add(raceBoardPanel);
            
            if(logoAndTitlePanel != null) {
                logoAndTitlePanel.setTitle(currentLiveRace.getRaceName());
            }

            activeTvView = TVViews.Raceboard;
        }
    }

    private FlowPanel createTimePanel(RaceBoardPanel raceBoardPanel) {
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
    
    public void updateTvView(TVViews tvView) {
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
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        if (currentLiveRace != null) {
            RaceTimesInfoDTO currentRaceTimes = raceTimesInfo.get(currentLiveRace);

            Date endOfRace = currentRaceTimes.endOfRace;
            long timeAfterEndOfRace = 3 * 60 * 1000; // 3 min  
            if (endOfRace != null && timer.getTime().getTime() > endOfRace.getTime() + timeAfterEndOfRace
                && timer.getPlayMode() == PlayModes.Live) {
                updateTvView(TVViews.Leaderboard);
            }
        } else {
            currentLiveRace = checkForLiveRace();
            if (currentLiveRace != null) {
                updateTvView(TVViews.Raceboard);
            } else {
                updateTvView(TVViews.Leaderboard);
            }
        }
    }

    private RegattaAndRaceIdentifier checkForLiveRace() {
        RegattaAndRaceIdentifier result = null;
                        return raceIdentifier;
        return result;
}
