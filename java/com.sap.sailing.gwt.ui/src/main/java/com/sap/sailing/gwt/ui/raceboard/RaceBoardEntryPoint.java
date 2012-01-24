package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;
    private RaceBoardPanel raceBoardPanel;

    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();

        final String eventName = Window.Location.getParameter("eventName");
        final String raceName = Window.Location.getParameter("raceName");
        String leaderboardNameParamValue = Window.Location.getParameter("leaderboardName");
        final String leaderboardName;
        if(leaderboardNameParamValue == null || leaderboardNameParamValue.isEmpty()) {
            leaderboardName = DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME;
        } else {
            leaderboardName = leaderboardNameParamValue;
            sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> leaderboardNames) {
                    if (!leaderboardNames.contains(leaderboardName)) {
                        createErrorPage(stringMessages.noSuchLeaderboard());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    reportError("Error trying to obtain the list of leaderboard names: " + t.getMessage());
                }
            });
        }
        
        if (eventName != null && !eventName.isEmpty() && raceName != null && !raceName.isEmpty()) {
            sailingService.listEvents(false, new AsyncCallback<List<EventDTO>>() {
                @Override
                public void onSuccess(List<EventDTO> events) {
                    selectedRace = findRace(eventName, raceName, events);
                    if(selectedRace != null) {
                        createRaceBoardPanel(selectedRace, events, eventName, leaderboardName);
                    } else {
                        createErrorPage("Could not obtain a race with name " + raceName + " for an event with name " + eventName);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    reportError("Error trying to obtain the list of events: " + t.getMessage());
                }
            });
        } else {
            createErrorPage("This page requires a valid event name and race name.");
        }
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

    private void createRaceBoardPanel(RaceDTO selectedRace, List<EventDTO> events, String eventName, String leaderboardName) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RaceIdentifier> singletonList = Collections.singletonList(selectedRace.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        raceBoardPanel = new RaceBoardPanel(sailingService, raceSelectionModel, leaderboardName,
                RaceBoardEntryPoint.this, stringMessages);
        raceBoardPanel.fillEvents(events);
        String padding = Window.Location.getParameter("padding");
        if (padding != null && Boolean.valueOf(padding)) {
            raceBoardPanel.addStyleName("leftPaddedPanel");
        }

        FlowPanel raceBoardHeaderPanel = new FlowPanel();
        raceBoardHeaderPanel.addStyleName("RaceBoardHeaderPanel");
        raceBoardHeaderPanel.add(raceBoardPanel.getHeaderWidget());
        
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
        
        FlowPanel footerShadowPanel = new FlowPanel();
        footerShadowPanel.addStyleName("footerShadowPanel");
        
        RootPanel.get().add(raceBoardHeaderPanel);        
        RootPanel.get().add(contentOuterPanel);
        
        // Don't change this order because of the inner logic in html of "position fixed"-elements
        RootPanel.get().add(logoAndTitlePanel);                 // position:fixed        
        RootPanel.get().add(timelinePanel);                     // position:fixed
        RootPanel.get().add(footerShadowPanel);                 // position:fixed
    }
}
