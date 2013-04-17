package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.panels.UserStatusPanel;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher {
    private Set<RegattaDisplayer> regattaDisplayers;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        UserStatusPanel userStatusPanel = new UserStatusPanel(userManagementService, this);
        userStatusPanel.ensureDebugId("UserStatus");
        rootPanel.add(userStatusPanel);
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.ensureDebugId("AdministrationTabs");
        tabPanel.setAnimationEnabled(true);
        tabPanel.setSize("95%", "95%");
        rootPanel.add(tabPanel); //, 10, 10);

        regattaDisplayers = new HashSet<RegattaDisplayer>();

        SailingEventManagementPanel sailingEventManagementPanel = new SailingEventManagementPanel(sailingService, this, stringMessages);
        tabPanel.add(sailingEventManagementPanel, stringMessages.events(), false);

        RegattaStructureManagementPanel eventStructureManagementPanel = new RegattaStructureManagementPanel(sailingService, this, stringMessages, this);
        //eventStructureManagementPanel.ensureDebugId("RegattaStructureManagement");
        tabPanel.add(eventStructureManagementPanel, stringMessages.regattas());
        regattaDisplayers.add(eventStructureManagementPanel);
        
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService, this, this, stringMessages);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        tractracEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(tractracEventManagementPanel, stringMessages.tracTracEvents(), false);
        regattaDisplayers.add(tractracEventManagementPanel);
        
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(sailingService, this, this, stringMessages);
        swissTimingReplayConnectorPanel.setSize("90%", "90%");
        tabPanel.add(swissTimingReplayConnectorPanel, stringMessages.swissTimingArchiveConnector(), false);
        regattaDisplayers.add(swissTimingReplayConnectorPanel);
        
        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(sailingService, this, this, stringMessages);
        //swisstimingEventManagementPanel.ensureDebugId("SwissTimingEventManagement");
        swisstimingEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), false);
        regattaDisplayers.add(swisstimingEventManagementPanel);
        
        CreateSwissTimingRacePanel createSwissTimingRacePanel = new CreateSwissTimingRacePanel(sailingService, this, stringMessages);
        //createSwissTimingRacePanel.ensureDebugId("CreateSwissTimingRace");
        createSwissTimingRacePanel.setSize("90%", "90%");
        tabPanel.add(createSwissTimingRacePanel,"Create SwissTiming race",false);
        
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this, this, stringMessages);
        //trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        trackedRacesManagementPanel.setSize("90%", "90%");
        tabPanel.add(trackedRacesManagementPanel, stringMessages.trackedRaces(),false);
        regattaDisplayers.add(trackedRacesManagementPanel);
        
        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this, stringMessages);
        //raceCourseManagementPanel.ensureDebugId("RaceCourseManagement");
        raceCourseManagementPanel.setSize("90%", "90%");
        tabPanel.add(raceCourseManagementPanel, stringMessages.courseLayout(),false);
        regattaDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        
        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        //windPanel.ensureDebugId("WindManagement");
        regattaDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        tabPanel.add(windPanel, stringMessages.wind(), /* asHTML */ false);
        
        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService, this, this, stringMessages);
        //leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        leaderboardGroupConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardGroupConfigPanel, stringMessages.leaderboardGroupConfiguration(), /*asHTML*/ false);
        regattaDisplayers.add(leaderboardGroupConfigPanel);
        
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this, stringMessages,
                /* showRaceDetails */ true);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        leaderboardConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), /* asHTML */ false);
        regattaDisplayers.add(leaderboardConfigPanel);
        
        tabPanel.add(new ResultImportUrlsManagementPanel(sailingService, this, stringMessages), stringMessages.resultImportUrls(), /* asHTML */ false);
        
        ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
        //replicationPanel.ensureDebugId("ReplicationManagement");
        tabPanel.add(replicationPanel, stringMessages.replication(), /* asHTML */ false);
        final MediaPanel mediaPanel = new MediaPanel(mediaService, this, stringMessages);
        tabPanel.add(mediaPanel, stringMessages.mediaPanel(), /* asHTML */ false);
        
        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (leaderboardConfigPanel.isVisible()) {
                    leaderboardConfigPanel.loadAndRefreshLeaderboards();
                }
                if (mediaPanel.isVisible()) {
                    mediaPanel.onShow();
                }
            }
        });
        fillRegattas();
    }

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onSuccess(List<RegattaDTO> result) {
                for (RegattaDisplayer regattaDisplayer : regattaDisplayers) {
                    regattaDisplayer.fillRegattas(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                reportError("Remote Procedure Call getRegattas() - Failure");
            }
        });
    }
}
