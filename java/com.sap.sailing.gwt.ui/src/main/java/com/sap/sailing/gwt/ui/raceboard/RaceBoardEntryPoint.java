
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentChecker;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;

    private String regattaName;
    private String raceName;
    private String leaderboardName;
    private String leaderboardGroupName;
    private RaceBoardViewModes viewMode;

    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        regattaName = Window.Location.getParameter("regattaName");
        raceName = Window.Location.getParameter("raceName");
        String leaderboardNameParamValue = Window.Location.getParameter("leaderboardName");
        String leaderboardGroupNameParamValue = Window.Location.getParameter("leaderboardGroupName");
        String viewModeParamValue = Window.Location.getParameter("viewMode");
        // set the view mode for the race board 
        if (viewModeParamValue != null && !viewModeParamValue.isEmpty()) {
            try {
                viewMode = RaceBoardViewModes.valueOf(viewModeParamValue);
            } catch (IllegalArgumentException e) {
                viewMode = RaceBoardViewModes.ONESCREEN;
            }
        } else {
            viewMode = RaceBoardViewModes.ONESCREEN;
        }
        if (leaderboardNameParamValue == null || leaderboardNameParamValue.isEmpty()) {
            leaderboardName = LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME;
        } else {
            leaderboardName = leaderboardNameParamValue;
        }
        if (leaderboardGroupNameParamValue != null && !leaderboardGroupNameParamValue.isEmpty()) {
            leaderboardGroupName = leaderboardGroupNameParamValue; 
        }
        if (regattaName == null || regattaName.isEmpty() || raceName == null || raceName.isEmpty()) {
            createErrorPage("This page requires a valid regatta name and race name.");
            return;
        }
        final ParallelExecutionCallback<List<String>> getLeaderboardNamesCallback = new ParallelExecutionCallback<List<String>>();  
        final ParallelExecutionCallback<List<RegattaDTO>> getRegattasCallback = new ParallelExecutionCallback<List<RegattaDTO>>();  
        final ParallelExecutionCallback<LeaderboardGroupDTO> getLeaderboardGroupByNameCallback = new ParallelExecutionCallback<LeaderboardGroupDTO>();  
        final ParallelExecutionCallback<UserDTO> getUserCallback = new ParallelExecutionCallback<UserDTO>();  
        if (leaderboardGroupName != null) {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, getLeaderboardGroupByNameCallback, getRegattasCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(),
                            getLeaderboardGroupByNameCallback.getData(), getRegattasCallback.getData(), getUserCallback.getData());
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        } else {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, getRegattasCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(), null, getRegattasCallback.getData(), getUserCallback.getData());
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        }
        sailingService.getRegattas(getRegattasCallback);
        sailingService.getLeaderboardNames(getLeaderboardNamesCallback);
        if (leaderboardGroupName != null) {
            sailingService.getLeaderboardGroupByName(leaderboardGroupNameParamValue, false /*withGeoLocationData*/, getLeaderboardGroupByNameCallback);
        }
        userManagementService.getUser(getUserCallback);
    }

    private void checkUrlParameters(List<String> leaderboardNames, LeaderboardGroupDTO leaderboardGroup, List<RegattaDTO> regattas, UserDTO user) {
        if (!leaderboardNames.contains(leaderboardName)) {
          createErrorPage(stringMessages.noSuchLeaderboard());
          return;
        }
        if (leaderboardGroupName != null && leaderboardGroup != null) {
            boolean foundLeaderboard = false; 
            for(StrippedLeaderboardDTO leaderBoard:  leaderboardGroup.leaderboards) {
                if(leaderBoard.name.equals(leaderboardName)) {
                    foundLeaderboard = true;
                    break;
                }
            }
            if (!foundLeaderboard) {
                createErrorPage("the leaderboard is not contained in this leaderboard group.");
                return;
            }
        }
        selectedRace = findRace(regattaName, raceName, regattas);
        if (selectedRace == null) {
            createErrorPage("Could not obtain a race with name " + raceName + " for a regatta with name " + regattaName);
            return;
        }

        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RegattaAndRaceIdentifier> singletonList = Collections.singletonList(selectedRace.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        Timer timer = new Timer(PlayModes.Replay, 1000l);
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, this, singletonList, 5000l /* requestInterval*/);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, user, timer, raceSelectionModel, leaderboardName, leaderboardGroupName,
                RaceBoardEntryPoint.this, stringMessages, userAgent, viewMode, raceTimesInfoProvider);
        raceBoardPanel.fillRegattas(regattas);

        switch (viewMode) {
            case ONESCREEN:
                createRaceBoardInOneScreenMode(raceBoardPanel);
                break;
        }
    }  

    private RaceDTO findRace(String regattaName, String raceName, List<RegattaDTO> regattas) {
        for (RegattaDTO regattaDTO : regattas) {
            if (regattaDTO.name.equals(regattaName)) {
                for (RaceDTO raceDTO : regattaDTO.races) {
                    if (raceDTO.name.equals(raceName)) {
                        return raceDTO;
                    }
                }
            }
        }
        return null;
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

    private FlowPanel createLogoAndTitlePanel(RaceBoardPanel raceBoardPanel) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(regattaName, selectedRace.name, stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, leaderboardName, leaderboardGroupName);
        logoAndTitlePanel.add(globalNavigationPanel);
        
        return logoAndTitlePanel;
    }
    
    private void createRaceBoardInOneScreenMode(RaceBoardPanel raceBoardPanel) {
        DockLayoutPanel p = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(p);
        FlowPanel toolbarPanel = new FlowPanel();
        toolbarPanel.add(raceBoardPanel.getNavigationWidget());
        if (!UserAgentChecker.INSTANCE.isUserAgentSupported(userAgent)) {
            HTML lbl = new HTML("This website is optimized to work with Google Chrome. <a target='_blank' href='https://www.google.com/intl/de/chrome/browser/'>Click here to download</a>");
            lbl.setStyleName("browserOptimizedMessage");
            toolbarPanel.add(lbl);
        }
        FlowPanel logoAndTitlePanel = createLogoAndTitlePanel(raceBoardPanel);
        FlowPanel timePanel = createTimePanel(raceBoardPanel);
        p.addNorth(logoAndTitlePanel, 68);        
        p.addNorth(toolbarPanel, 40);
        p.addSouth(timePanel, 90);                     
        p.add(raceBoardPanel);
        p.addStyleName("dockLayoutPanel");
    }    
}
