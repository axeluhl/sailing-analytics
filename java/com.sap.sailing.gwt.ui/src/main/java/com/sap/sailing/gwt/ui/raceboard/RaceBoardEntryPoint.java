package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.server.api.DefaultLeaderboardName;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;
    private String eventName;
    private String raceName;
    private String leaderboardName;

    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();

        eventName = Window.Location.getParameter("eventName");
        raceName = Window.Location.getParameter("raceName");
        leaderboardName = Window.Location.getParameter("leaderboardName");
        
        if(leaderboardName == null || leaderboardName.isEmpty()) {
            leaderboardName = DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME;
        } else {
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
        
        if(eventName != null && !eventName.isEmpty() && raceName != null && !raceName.isEmpty()) {
            sailingService.listEvents(false, new AsyncCallback<List<EventDTO>>() {
                @Override
                public void onSuccess(List<EventDTO> eventNames) {
                    selectedRace = findRace(eventNames);
                    if(selectedRace != null)
                        createRaceBoardPanel(selectedRace);
                    else
                        createErrorPage("Could not obtain a race with name " + raceName + " for an event with name " + eventName);
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

    private RaceDTO findRace(List<EventDTO> eventNames) {
        for (EventDTO eventDTO : eventNames) {
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

    private void createRaceBoardPanel(RaceDTO selectedRace)
    {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, selectedRace, leaderboardName,
                RaceBoardEntryPoint.this, stringMessages);
        String padding = Window.Location.getParameter("padding");
        if (padding != null && Boolean.valueOf(padding)) {
            raceBoardPanel.addStyleName("leftPaddedPanel");
        }
        RootPanel.get().add(logoAndTitlePanel);
        RootPanel.get().add(raceBoardPanel);
    }
}
