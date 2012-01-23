package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.server.api.DefaultLeaderboardName;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;

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
                        createRaceBoardPanel(selectedRace, eventName, leaderboardName);
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

    private void createRaceBoardPanel(RaceDTO selectedRace, String eventName, String leaderboardName) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, selectedRace, leaderboardName,
                RaceBoardEntryPoint.this, stringMessages);
        String padding = Window.Location.getParameter("padding");
        if (padding != null && Boolean.valueOf(padding)) {
            raceBoardPanel.addStyleName("leftPaddedPanel");
        }
        FlowPanel contentOuterPanel = new FlowPanel(); // outer div which centered page content
        contentOuterPanel.addStyleName("contentOuterPanel");
        contentOuterPanel.add(raceBoardPanel);
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.addStyleName("timelinePanel");
        FlowPanel timelineInnerPanel = new FlowPanel();
        timelineInnerPanel.addStyleName("timelineInnerPanel");
        FlowPanel footerShadowPanel = new FlowPanel();
        footerShadowPanel.addStyleName("footerShadowPanel");
        FlowPanel breadcrumbPanel = new FlowPanel();
        breadcrumbPanel.addStyleName("breadcrumbPanel");
        Label eventNameLabel = new Label(eventName);
        eventNameLabel.addStyleName("eventNameHeadline");
        breadcrumbPanel.add(eventNameLabel);
        timelinePanel.add(timelineInnerPanel);
        RootPanel.get().add(breadcrumbPanel);
        RootPanel.get().add(contentOuterPanel);
        // Don't change this order because of the inner logic in html of "position fixed"-elements
        RootPanel.get().add(logoAndTitlePanel); // position:fixed
        RootPanel.get().add(timelinePanel); // position:fixed
        RootPanel.get().add(footerShadowPanel); // position:fixed
    }
}
