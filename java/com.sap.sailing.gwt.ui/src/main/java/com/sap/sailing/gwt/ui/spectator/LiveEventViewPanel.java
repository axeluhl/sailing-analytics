package com.sap.sailing.gwt.ui.spectator;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.adminconsole.RaceMap;
import com.sap.sailing.gwt.ui.adminconsole.TimePanel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;

public class LiveEventViewPanel extends AbstractEventPanel {
    
    private LeaderboardDTO leaderboard = null;
    private EventDTO actualEvent = null;
    private RaceDTO actualRace = null;
    
    private Timer timer = null;
    private TimePanel timePanel = null;

    public LiveEventViewPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, StringMessages stringConstants, String leaderboardName, String eventName) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        
        //Get leaderboard and event from server. The method which needs longer calls the method to get the actual race and to build the GUI
        fillLeaderboard(leaderboardName);
        fillActualEvent(eventName);
    }
    
    private void buildGUI() {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        mainPanel.setWidth("95%");
        setWidget(mainPanel);

        //Create leaderboard
        CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(true);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, competitorSelectionModel, leaderboard.name, errorReporter, stringConstants);
        
        CollapsablePanel leaderboardCollapsablePanel = new CollapsablePanel(stringConstants.leaderboard() + ": " + leaderboard.name);
        leaderboardCollapsablePanel.setWidth("100%");
        leaderboardCollapsablePanel.setContent(leaderboardPanel);
        
        mainPanel.add(leaderboardCollapsablePanel);
        
        //Create race map
        timer = new Timer(500);
        timer.pause();
        
        VerticalPanel raceMapContent = new VerticalPanel();
        
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringConstants);
        SimplePanel raceMapPanel = new SimplePanel();
        raceMapPanel.setSize("800px", "600px");
        raceMap.loadMapsAPI(raceMapPanel);
        raceMap.onRaceSelectionChange(Collections.singletonList(actualRace.getRaceIdentifier()));
        raceMapContent.add(raceMapPanel);
        
//        timePanel = new TimePanel(stringConstants, timer);
//        if (actualRace.startOfRace != null) {
//            timePanel.setMin(actualRace.startOfRace);
//        } else {
//            timePanel.setMin(new Date());
//        }
//        //TODO How to set the maximum of the time panel for a live Event
//        if (actualRace.timePointOfNewestEvent != null) {
//            timePanel.setMax(actualRace.timePointOfNewestEvent);
//        } else {
//            timePanel.setMax(new Date(new Date().getTime() + 4 * 60 * 60 * 1000));
//        }
//        raceMapContent.add(timePanel);
        
        CollapsablePanel raceMapCollapsablePanel = new CollapsablePanel(stringConstants.map() + ": " + actualRace.name);
        raceMapCollapsablePanel.setWidth("100%");
        raceMapCollapsablePanel.setContent(raceMapContent);
        mainPanel.add(raceMapCollapsablePanel);
        
        timer.addTimeListener(raceMap);
        timer.addTimeListener(leaderboardPanel);
        //For testing the timer is setted to the start of the actual race. For productive use, set time to actual time
//        timer.setTime(new Date().getTime());
        timer.setTime(actualRace.startOfRace.getTime() + 10000);
        timer.resume();
    }
    
    private void fillLeaderboard(final String name) {
        sailingService.getLeaderboardByName(name, new Date(), null, new AsyncCallback<LeaderboardDTO>() {
            @Override
            public void onSuccess(LeaderboardDTO leaderboardDTO) {
                leaderboard = leaderboardDTO;
                if (actualEvent != null) {
                    fillActualRace();
                    buildGUI();
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to fetch leaderboard " + name
                        + " from the server: " + caught.getMessage());
            }
        });
    }
    
    private void fillActualEvent(final String name) {
        sailingService.listEvents(false, new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to fetch event " + name
                        + " from the server: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> events) {
                for (EventDTO event : events) {
                    if (event.name.equals(name)) {
                        actualEvent = event;
                        break;
                    }
                }
                if (leaderboard != null) {
                    fillActualRace();
                    buildGUI();
                }
            }
        });
    }
    
    private void fillActualRace() {
        if (leaderboard != null && actualEvent != null) {
            for (RegattaDTO regatta : actualEvent.regattas) {
                for (RaceDTO race : regatta.races) {
                    if (race.currentlyTracked && isRaceInLeaderboard(race)) {
                        if (actualRace == null) {
                            actualRace = race;
                        } else {
                            actualRace = actualRace.startOfRace.compareTo(race.startOfRace) > 0 ? actualRace : race;
                        }
                    }
                }
            }
        }
    }
    
    private boolean isRaceInLeaderboard(RaceDTO race) {
        boolean result = false;
        
        for (RaceInLeaderboardDTO raceInLeaderboard : leaderboard.getRaceList()) {
            RaceIdentifier raceId = raceInLeaderboard.getRaceIdentifier();
            if (raceId != null && race.name.equals(raceId.getRaceName())) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
    }

}
