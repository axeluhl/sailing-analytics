package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.panels.UserStatusPanel;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher {
    private Set<RegattaDisplayer> regattaDisplayers;
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        rootPanel.add(new UserStatusPanel(userManagementService, this));
        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        rootPanel.add(tabPanel); //, 10, 10);
        tabPanel.setSize("95%", "95%");

        regattaDisplayers = new HashSet<RegattaDisplayer>();
        
        EventStructureManagementPanel eventStructureManagementPanel = new EventStructureManagementPanel(sailingService, this, stringMessages, this);
        tabPanel.add(eventStructureManagementPanel, stringMessages.regattas());
        regattaDisplayers.add(eventStructureManagementPanel);
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService, this, this, stringMessages);
        regattaDisplayers.add(tractracEventManagementPanel);
        tractracEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(tractracEventManagementPanel, stringMessages.tracTracEvents(), false);
        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(sailingService, this, this, stringMessages);
        regattaDisplayers.add(swisstimingEventManagementPanel);
        swisstimingEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), false);
        CreateSwissTimingRacePanel createSwissTimingRacePanel = new CreateSwissTimingRacePanel(sailingService,this,stringMessages);
        createSwissTimingRacePanel.setSize("90%", "90%");
        tabPanel.add(createSwissTimingRacePanel,"Create SwissTiming race",false);
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this, this, stringMessages);
        regattaDisplayers.add(trackedRacesManagementPanel);
        trackedRacesManagementPanel.setSize("90%", "90%");
        tabPanel.add(trackedRacesManagementPanel, stringMessages.trackedRaces(),false);
        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        regattaDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        tabPanel.add(windPanel, stringMessages.wind(), /* asHTML */ false);
        LeaderboardSettings defaultLeaderboardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettings(/* racesToShow */ null, /* namesOfRacesToShow */ null, null, /* autoExpandFirstRace */false);
        final LeaderboardPanel defaultLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                defaultLeaderboardSettings,
                /* preSelectedRace */null, new CompetitorSelectionModel(/* hasMultiSelection */true),
                DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME, /* leaderboard group name */null, this,
                stringMessages, userAgentType);
        defaultLeaderboardPanel.setSize("90%", "90%");
        tabPanel.add(defaultLeaderboardPanel, stringMessages.defaultLeaderboard(), /* asHTML */ false);
        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService, this, this, stringMessages);
        leaderboardGroupConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardGroupConfigPanel, stringMessages.leaderboardGroupConfiguration(), /*asHTML*/ false);
        regattaDisplayers.add(leaderboardGroupConfigPanel);
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this, stringMessages);
        leaderboardConfigPanel.setSize("90%", "90%");
        tabPanel.add(leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), /* asHTML */ false);
        regattaDisplayers.add(leaderboardConfigPanel);
        tabPanel.add(new ReplicationPanel(sailingService, this, stringMessages), stringMessages.replication(), /* asHTML */ false);
        
        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (leaderboardConfigPanel.isVisible()) {
				    leaderboardConfigPanel.loadAndRefreshAllData();
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
