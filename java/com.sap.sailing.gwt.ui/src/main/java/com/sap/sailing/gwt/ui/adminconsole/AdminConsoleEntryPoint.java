package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.UserManagementService;
import com.sap.sailing.gwt.ui.client.UserManagementServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.panels.SystemInformationPanel;
import com.sap.sailing.gwt.ui.client.shared.panels.UserStatusPanel;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);

        userManagementService.getUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                reportError("Could not read user: " + caught.getMessage());
            }

            @Override
            public void onSuccess(UserDTO result) {
                createUI(result);
            }
        });
    }
     
    private void createUI(UserDTO user) {
        BetterDateTimeBox.initialize();
        
        final VerticalTabLayoutPanel tabPanel = new VerticalTabLayoutPanel(2.5, Unit.EM);
        tabPanel.ensureDebugId("AdministrationTabs");
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();

        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(tabPanel, user, eventManagementPanel, stringMessages.events(), AdminConsoleFeatures.MANAGE_EVENTS);
        leaderboardGroupsDisplayers.add(eventManagementPanel);

        RegattaStructureManagementPanel regattaManagementPanel = new RegattaStructureManagementPanel(
                sailingService, this, stringMessages, this);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        addToTabPanel(tabPanel, user, regattaManagementPanel, stringMessages.regattas(), AdminConsoleFeatures.MANAGE_REGATTAS);
        regattasDisplayers.add(regattaManagementPanel);
        
        final TabLayoutPanel leaderboardTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        leaderboardTabPanel.ensureDebugId("LeaderboardPanel");
        tabPanel.add(leaderboardTabPanel, stringMessages.leaderboards());
        
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                stringMessages, /* showRaceDetails */true, this);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        addToTabPanel(leaderboardTabPanel, user, leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), AdminConsoleFeatures.MANAGE_LEADERBOARDS);
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, this, this, stringMessages);
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        addToTabPanel(leaderboardTabPanel, user, leaderboardGroupConfigPanel, stringMessages.leaderboardGroups(), AdminConsoleFeatures.MANAGE_LEADERBOARD_GROUPS);
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);

        final TabLayoutPanel racesTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        racesTabPanel.ensureDebugId("RacesPanel");
        tabPanel.add(racesTabPanel, stringMessages.races());
        
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, stringMessages);
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        addToTabPanel(racesTabPanel, user, trackedRacesManagementPanel, stringMessages.trackedRaces(), AdminConsoleFeatures.SHOW_TRACKED_RACES);
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, stringMessages, this);
        addToTabPanel(racesTabPanel, user, competitorPanel, stringMessages.competitors(), AdminConsoleFeatures.MANAGE_ALL_COMPETITORS);

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this, stringMessages);
        // raceCourseManagementPanel.ensureDebugId("RaceCourseManagement");
        addToTabPanel(racesTabPanel, user, raceCourseManagementPanel, stringMessages.courseLayout(), AdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        // windPanel.ensureDebugId("WindManagement");
        regattasDisplayers.add(windPanel);
        addToTabPanel(racesTabPanel, user, windPanel, stringMessages.wind(), AdminConsoleFeatures.MANAGE_WIND);

        final MediaPanel mediaPanel = new MediaPanel(mediaService, this, stringMessages);
        addToTabPanel(racesTabPanel, user, mediaPanel, stringMessages.mediaPanel(), AdminConsoleFeatures.MANAGE_MEDIA);

        /*final DeviceConfigurationPanel deviceConfigurationAdminPanel = new DeviceConfigurationPanel(sailingService,
                stringMessages, this);
        addScrollableTab(tabPanel, deviceConfigurationAdminPanel, stringMessages.deviceConfiguration() + " (admin)");*/
        
        final TabLayoutPanel connectorsTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        connectorsTabPanel.ensureDebugId("TrackingProviderPanel");
        tabPanel.add(connectorsTabPanel, stringMessages.connectors());

        AbstractEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, stringMessages);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        addToTabPanel(connectorsTabPanel, user, tractracEventManagementPanel, stringMessages.tracTracEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, user, swissTimingReplayConnectorPanel, stringMessages.swissTimingArchiveConnector(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, user, swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final RaceLogTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new RaceLogTrackingEventManagementPanel(
                sailingService, this, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, user, raceLogTrackingEventManagementPanel, stringMessages.raceLogTracking(), AdminConsoleFeatures.MANAGE_RACELOG_TRACKING);
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, stringMessages);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        addToTabPanel(connectorsTabPanel, user, igtimiAccountsPanel, stringMessages.igtimiAccounts(), AdminConsoleFeatures.MANAGE_IGTIMI_ACCOUNTS);
        
        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(connectorsTabPanel, user, resultImportUrlsManagementPanel, stringMessages.resultImportUrls(), AdminConsoleFeatures.MANAGE_RESULT_IMPORT_URLS);

        final TabLayoutPanel advancedTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        advancedTabPanel.ensureDebugId("AdvancedPanel");
        tabPanel.add(advancedTabPanel, "Advanced");

        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
        // replicationPanel.ensureDebugId("ReplicationManagement");
        addToTabPanel(advancedTabPanel, user, replicationPanel, stringMessages.replication(), AdminConsoleFeatures.MANAGE_REPLICATION);

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(stringMessages, sailingService,
                this, eventManagementPanel, this, this);
        addToTabPanel(advancedTabPanel, user, masterDataImportPanel, stringMessages.masterDataImportPanel(), AdminConsoleFeatures.MANAGE_MASTERDATA_IMPORT);

        RemoteSailingServerInstancesManagementPanel sailingServerInstancesManagementPanel = new RemoteSailingServerInstancesManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(advancedTabPanel, user, sailingServerInstancesManagementPanel, stringMessages.sailingServers(), AdminConsoleFeatures.MANAGE_SAILING_SERVER_INSTANCES);

        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                stringMessages, this);
        addToTabPanel(advancedTabPanel, user, deviceConfigurationUserPanel, stringMessages.deviceConfiguration(), AdminConsoleFeatures.MANAGE_DEVICE_CONFIGURATION);

        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                final Widget selectedPanel = tabPanel.getWidget(tabPanel.getSelectedIndex());
                if (selectedPanel == eventManagementPanel) {
                    eventManagementPanel.fillEvents();
                    fillLeaderboardGroups();
                } else if (selectedPanel == leaderboardConfigPanel) {
                    fillLeaderboards();
                } else if (selectedPanel == mediaPanel) {
                    mediaPanel.onShow();
                } else if (selectedPanel == competitorPanel) {
                    competitorPanel.refreshCompetitorList();
                } else if (selectedPanel == raceLogTrackingEventManagementPanel) {
                    fillLeaderboards();
                } else if (selectedPanel == leaderboardGroupConfigPanel) {
                    fillLeaderboardGroups();
                    fillLeaderboards();
                }
            }
        });
        
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();

        DockPanel informationPanel = new DockPanel();
        informationPanel.setSize("100%", "95%");
        informationPanel.setSpacing(10);
        UserStatusPanel userStatusPanel = new UserStatusPanel(user);
        userStatusPanel.ensureDebugId("UserStatus");
        informationPanel.add(userStatusPanel, DockPanel.WEST);
        informationPanel.add(persistentAlertLabel, DockPanel.CENTER);

        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(sailingService, this);
        sysinfoPanel.ensureDebugId("SystemInformation");

        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(stringMessages.releaseNotes()).toSafeHtml(), "/release_notes_admin.html");
        sysinfoPanel.add(releaseNotesLink);
        informationPanel.add(sysinfoPanel, DockPanel.EAST);
        informationPanel.setCellHorizontalAlignment(sysinfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);

        dockPanel.addSouth(informationPanel, 2.5);
        dockPanel.add(tabPanel);
        rootPanel.add(dockPanel);
    }

    private void addToTabPanel(VerticalTabLayoutPanel tabPanel, UserDTO user, Panel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        if(user != null && isUserInRole(user, feature.getEnabledRoles())) {
            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.add(panelToAdd);
            panelToAdd.setSize("90%", "90%");
            tabPanel.add(scrollPanel, tabTitle, false);
        }
    }
    
    private void addToTabPanel(TabLayoutPanel tabPanel, UserDTO user, Panel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        if(user != null && isUserInRole(user, feature.getEnabledRoles())) {
            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.add(panelToAdd);
            panelToAdd.setSize("90%", "90%");
            tabPanel.add(scrollPanel, tabTitle, false);
        }
    }
    
    private boolean isUserInRole(UserDTO user, UserRoles[] roles) {
        boolean result = false;
        for(UserRoles enabledRole: roles) {
            if (user.roles.contains(enabledRole.name())) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    @Override
    public void fillLeaderboards() {
        sailingService.getLeaderboards(new MarkedAsyncCallback<List<StrippedLeaderboardDTO>>(
                new AsyncCallback<List<StrippedLeaderboardDTO>>() {
                    @Override
                    public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                        for (LeaderboardsDisplayer leaderboardsDisplayer : leaderboardsDisplayers) {
                            leaderboardsDisplayer.fillLeaderboards(leaderboards);
                        }
                    }
        
                    @Override
                    public void onFailure(Throwable t) {
                        reportError("Error trying to obtain list of leaderboards: "+ t.getMessage());
                    }
                }));
    }
    
    @Override
    public void updateLeaderboards(Iterable<StrippedLeaderboardDTO> updatedLeaderboards, LeaderboardsDisplayer origin) {
        for (LeaderboardsDisplayer leaderboardsDisplayer : leaderboardsDisplayers) {
            if (leaderboardsDisplayer != origin) {
                leaderboardsDisplayer.fillLeaderboards(updatedLeaderboards);
            }
        }
    }

    @Override
    public void fillLeaderboardGroups() {
        sailingService.getLeaderboardGroups(false /*withGeoLocationData*/,
                new MarkedAsyncCallback<List<LeaderboardGroupDTO>>(
                        new AsyncCallback<List<LeaderboardGroupDTO>>() {
                            @Override
                            public void onSuccess(List<LeaderboardGroupDTO> groups) {
                                for (LeaderboardGroupsDisplayer leaderboardGroupsDisplayer : leaderboardGroupsDisplayers) {
                                    leaderboardGroupsDisplayer.fillLeaderboardGroups(groups);
                                }
                            }
                            @Override
                            public void onFailure(Throwable t) {
                                reportError("Error trying to obtain list of leaderboard groups: " + t.getMessage());
                            }
                        }));
    }

    @Override
    public void updateLeaderboardGroups(Iterable<LeaderboardGroupDTO> updatedLeaderboardGroups,
            LeaderboardGroupsDisplayer origin) {
        for (LeaderboardGroupsDisplayer leaderboardGroupsDisplayer : leaderboardGroupsDisplayers) {
            if (leaderboardGroupsDisplayer != origin) {
                leaderboardGroupsDisplayer.fillLeaderboardGroups(updatedLeaderboardGroups);
            }
        }
    }

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
                new AsyncCallback<List<RegattaDTO>>() {
                    @Override
                    public void onSuccess(List<RegattaDTO> result) {
                        for (RegattasDisplayer regattaDisplayer : regattasDisplayers) {
                            regattaDisplayer.fillRegattas(result);
                        }
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        reportError("Remote Procedure Call getRegattas() - Failure");
                    }
                }));
    }
}
