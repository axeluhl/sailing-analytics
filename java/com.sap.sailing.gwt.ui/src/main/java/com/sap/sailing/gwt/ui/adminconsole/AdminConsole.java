package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.server.api.DefaultLeaderboardName;

public class AdminConsole extends AbstractEntryPoint implements EventRefresher {
    private Set<EventDisplayer> eventDisplayers;
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("95%", "95%");
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        rootPanel.add(tabPanel, 10, 10);
        tabPanel.setSize("95%", "95%");
        
        eventDisplayers = new HashSet<EventDisplayer>();
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService, this, this, stringMessages);
        eventDisplayers.add(tractracEventManagementPanel);
        tractracEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(tractracEventManagementPanel, stringMessages.tracTracEvents(), false);
        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(sailingService, this, this, stringMessages);
        eventDisplayers.add(swisstimingEventManagementPanel);
        swisstimingEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), false);
        CreateSwissTimingRacePanel createSwissTimingRacePanel = new CreateSwissTimingRacePanel(sailingService,this,stringMessages);
        createSwissTimingRacePanel.setSize("90%", "90%");
        tabPanel.add(createSwissTimingRacePanel,"Create SwissTiming race",false);
        WindPanel windPanel = new WindPanel(sailingService, this, this, stringMessages);
        eventDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        tabPanel.add(windPanel, stringMessages.wind(), /* asHTML */ false);
        final RaceMapPanel raceMapPanel = new RaceMapPanel(sailingService, new CompetitorSelectionModel(
                /* hasMultiSelection */true), this, this, stringMessages);
        eventDisplayers.add(raceMapPanel);
        raceMapPanel.setSize("90%", "90%");
        tabPanel.add(raceMapPanel, stringMessages.map(), /* asHTML */ false);
        LeaderboardPanel defaultLeaderboardPanel = new LeaderboardPanel(sailingService, null,
                new CompetitorSelectionModel(/* hasMultiSelection */true),
        DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME, this, stringMessages);
        defaultLeaderboardPanel.setSize("90%", "90%");
        tabPanel.add(defaultLeaderboardPanel, stringMessages.defaultLeaderboard(), /* asHTML */ false);
        LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this, stringMessages);
        leaderboardConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), /* asHTML */ false);
        eventDisplayers.add(leaderboardConfigPanel);
        
        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if(raceMapPanel.isVisible()) {
					raceMapPanel.onResize();
				}				
			}
		});
        fillEvents();
    }

    @Override
    public void fillEvents() {
        sailingService.listEvents(false, new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> result) {
                for (EventDisplayer eventDisplayer : eventDisplayers) {
                    eventDisplayer.fillEvents(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                reportError("Remote Procedure Call listEvents() - Failure");
            }
        });
    }

}
