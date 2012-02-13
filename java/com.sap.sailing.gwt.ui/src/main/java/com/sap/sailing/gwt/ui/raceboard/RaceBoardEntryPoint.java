package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;
    private RaceBoardPanel raceBoardPanel;

    private String eventName;
    private String raceName;
    private String leaderboardName;
    private String leaderboardGroupName;

    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        
        eventName = Window.Location.getParameter("eventName");
        raceName = Window.Location.getParameter("raceName");
        String leaderboardNameParamValue = Window.Location.getParameter("leaderboardName");
        String leaderboardGroupNameParamValue = Window.Location.getParameter("leaderboardGroupName");

        if(leaderboardNameParamValue == null || leaderboardNameParamValue.isEmpty()) {
            leaderboardName = DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME;
        } else {
            leaderboardName = leaderboardNameParamValue;
        }

        if(leaderboardGroupNameParamValue != null && !leaderboardGroupNameParamValue.isEmpty())
            leaderboardGroupName = leaderboardGroupNameParamValue; 

        if (eventName == null || eventName.isEmpty() || raceName == null || raceName.isEmpty()) {
            createErrorPage("This page requires a valid event name and race name.");
            return;
        }
        
        final ParallelExecutionCallback<List<String>> getLeaderboardNamesCallback = new ParallelExecutionCallback<List<String>>();  
        final ParallelExecutionCallback<List<EventDTO>> listEventsCallback = new ParallelExecutionCallback<List<EventDTO>>();  
        final ParallelExecutionCallback<LeaderboardGroupDTO> getLeaderboardGroupByNameCallback = new ParallelExecutionCallback<LeaderboardGroupDTO>();  
        final ParallelExecutionCallback<UserDTO> getUserCallback = new ParallelExecutionCallback<UserDTO>();  
            
        if (leaderboardGroupName != null) {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, getLeaderboardGroupByNameCallback, listEventsCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(),
                            getLeaderboardGroupByNameCallback.getData(), listEventsCallback.getData(), getUserCallback.getData());
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        } else {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, listEventsCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(), null, listEventsCallback.getData(), getUserCallback.getData());
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        }
            
        sailingService.listEvents(false, listEventsCallback);
        sailingService.getLeaderboardNames(getLeaderboardNamesCallback);
        if(leaderboardGroupName != null)
            sailingService.getLeaderboardGroupByName(leaderboardGroupNameParamValue, getLeaderboardGroupByNameCallback);
        userManagementService.getUser(getUserCallback);
    }

    private void checkUrlParameters(List<String> leaderboardNames, LeaderboardGroupDTO leaderboardGroup, List<EventDTO> events, UserDTO user)
    {
        if (!leaderboardNames.contains(leaderboardName)) {
          createErrorPage(stringMessages.noSuchLeaderboard());
          return;
        }

        if (leaderboardGroupName != null && leaderboardGroup != null) {
            boolean foundLeaderboard = false; 
            for(LeaderboardDTO leaderBoard:  leaderboardGroup.leaderboards) {
                if(leaderBoard.name.equals(leaderboardName)) {
                    foundLeaderboard = true;
                    break;
                }
            }
            if(!foundLeaderboard) {
                createErrorPage("the leaderboard is not contained in this leaderboard group.");
                return;
            }
        }

        selectedRace = findRace(eventName, raceName, events);
        if(selectedRace == null) {
            createErrorPage("Could not obtain a race with name " + raceName + " for an event with name " + eventName);
            return;
        }
         
        createRaceBoardPanel(selectedRace, events, user);
    }  

    private RaceDTO findRace(String eventName, String raceName, List<EventDTO> events) {
        for (EventDTO eventDTO : events) {
            if(eventDTO.name.equals(eventName)) {
                for (RegattaDTO regattaDTO : eventDTO.regattas) {
                    for(RaceDTO raceDTO: regattaDTO.races) {
                        if(raceDTO.name.equals(raceName)) {
                            return raceDTO;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void createRaceBoardPanel(RaceDTO selectedRace, List<EventDTO> events, UserDTO userDTO) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RaceIdentifier> singletonList = Collections.singletonList(selectedRace.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        raceBoardPanel = new RaceBoardPanel(sailingService, userDTO, raceSelectionModel, leaderboardName, leaderboardGroupName,
                RaceBoardEntryPoint.this, stringMessages, userAgentType);
        raceBoardPanel.fillEvents(events);

        logoAndTitlePanel.add(raceBoardPanel.getNavigationWidget());
        
        FlowPanel raceBoardHeaderPanel = new FlowPanel();
        raceBoardHeaderPanel.addStyleName("RaceBoardHeaderPanel");
        raceBoardHeaderPanel.add(raceBoardPanel.getBreadcrumbWidget());
        
        FlowPanel contentOuterPanel = new FlowPanel(); // outer div which centered page content
        contentOuterPanel.addStyleName("contentOuterPanel");
        contentOuterPanel.add(raceBoardPanel);

        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimeWidget());
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        //FlowPanel footerShadowPanel = new FlowPanel();
        // footerShadowPanel.addStyleName("footerShadowPanel");
        
        RootPanel.get().add(raceBoardHeaderPanel);        
        RootPanel.get().add(contentOuterPanel);
        
        // Don't change this order because of the inner logic in html of "position fixed"-elements
        RootPanel.get().add(logoAndTitlePanel);                 // position:fixed        
        RootPanel.get().add(timelinePanel);                     // position:fixed
        //RootPanel.get().add(footerShadowPanel);                 // position:fixed
        raceBoardPanel.setScrollOffset(logoAndTitlePanel.getOffsetHeight());
    }
}
