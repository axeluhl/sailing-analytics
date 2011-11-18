package com.sap.sailing.gwt.ui.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

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
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(tractracEventManagementPanel);
        tractracEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(tractracEventManagementPanel, stringConstants.tracTracEvents(), false);
        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(swisstimingEventManagementPanel);
        swisstimingEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(swisstimingEventManagementPanel, stringConstants.swissTimingEvents(), false);
        WindPanel windPanel = new WindPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        tabPanel.add(windPanel, stringConstants.wind(), /* asHTML */ false);
        final RaceMapPanel raceMapPanel = new RaceMapPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(raceMapPanel);
        raceMapPanel.setSize("90%", "90%");
        tabPanel.add(raceMapPanel, stringConstants.map(), /* asHTML */ false);
        LeaderboardPanel defaultLeaderboardPanel = new LeaderboardPanel(sailingService, stringConstants.defaultLeaderboard(), this, stringConstants);
        defaultLeaderboardPanel.setSize("90%", "90%");
        tabPanel.add(defaultLeaderboardPanel, stringConstants.defaultLeaderboard(), /* asHTML */ false);
        LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this, stringConstants);
        leaderboardConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardConfigPanel, stringConstants.leaderboardConfiguration(), /* asHTML */ false);
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
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {
            @Override
            public void onSuccess(List<EventDAO> result) {
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
