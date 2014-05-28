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
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.UserManagementService;
import com.sap.sailing.gwt.ui.client.UserManagementServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.controls.ScrolledTabLayoutPanel;
import com.sap.sailing.gwt.ui.client.shared.panels.SystemInformationPanel;
import com.sap.sailing.gwt.ui.client.shared.panels.UserStatusPanel;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher {
    private Set<RegattaDisplayer> regattaDisplayers;

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

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
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        rootPanel.add(dockPanel);

        DockPanel topInformationPanel = new DockPanel();
        topInformationPanel.setSize("100%", "95%");
        UserStatusPanel userStatusPanel = new UserStatusPanel(user);
        userStatusPanel.ensureDebugId("UserStatus");
        topInformationPanel.add(userStatusPanel, DockPanel.WEST);
        topInformationPanel.add(persistentAlertLabel, DockPanel.CENTER);
        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(stringMessages.releaseNotes()).toSafeHtml(), "/release_notes_admin.html");
        topInformationPanel.add(releaseNotesLink, DockPanel.EAST);
        topInformationPanel.setCellHorizontalAlignment(releaseNotesLink, HasHorizontalAlignment.ALIGN_RIGHT);

        dockPanel.addNorth(topInformationPanel, 2.5);

        TabLayoutPanel tabPanel = new ScrolledTabLayoutPanel(2.5, Unit.EM, resources.arrowLeft(), resources.arrowRight(), 200);
        tabPanel.ensureDebugId("AdministrationTabs");
        regattaDisplayers = new HashSet<RegattaDisplayer>();

        SailingEventManagementPanel sailingEventManagementPanel = new SailingEventManagementPanel(sailingService, this,
                stringMessages);
        addToTabPanel(tabPanel, user, sailingEventManagementPanel, stringMessages.events(), AdminConsoleFeatures.MANAGE_EVENTS);

        RegattaStructureManagementPanel eventStructureManagementPanel = new RegattaStructureManagementPanel(
                sailingService, this, stringMessages, this);
        eventStructureManagementPanel.ensureDebugId("RegattaStructureManagement");
        addToTabPanel(tabPanel, user, eventStructureManagementPanel, stringMessages.regattas(), AdminConsoleFeatures.MANAGE_REGATTAS);
        regattaDisplayers.add(eventStructureManagementPanel);

        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, stringMessages);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        addToTabPanel(tabPanel, user, tractracEventManagementPanel, stringMessages.tracTracEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattaDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, stringMessages);
        addToTabPanel(tabPanel, user, swissTimingReplayConnectorPanel, stringMessages.swissTimingArchiveConnector(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattaDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, stringMessages);
        // swisstimingEventManagementPanel.ensureDebugId("SwissTimingEventManagement");
        addToTabPanel(tabPanel, user, swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattaDisplayers.add(swisstimingEventManagementPanel);

        final RaceLogTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new RaceLogTrackingEventManagementPanel(
                sailingService, this, this, this, stringMessages);
        addToTabPanel(tabPanel, user, raceLogTrackingEventManagementPanel, stringMessages.raceLogTracking(), AdminConsoleFeatures.MANAGE_RACELOG_TRACKING);
        regattaDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, stringMessages);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        addToTabPanel(tabPanel, user, igtimiAccountsPanel, stringMessages.igtimiAccounts(), AdminConsoleFeatures.MANAGE_IGTIMI_ACCOUNTS);

        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, stringMessages);
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        addToTabPanel(tabPanel, user, trackedRacesManagementPanel, stringMessages.trackedRaces(), AdminConsoleFeatures.SHOW_TRACKED_RACES);
        regattaDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, stringMessages, this);
        addToTabPanel(tabPanel, user, competitorPanel, stringMessages.competitors(), AdminConsoleFeatures.MANAGE_ALL_COMPETITORS);

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this,
                stringMessages);
        // raceCourseManagementPanel.ensureDebugId("RaceCourseManagement");
        addToTabPanel(tabPanel, user, raceCourseManagementPanel, stringMessages.courseLayout(), AdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattaDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        // windPanel.ensureDebugId("WindManagement");
        regattaDisplayers.add(windPanel);
        addToTabPanel(tabPanel, user, windPanel, stringMessages.wind(), AdminConsoleFeatures.MANAGE_WIND);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, stringMessages);
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        addToTabPanel(tabPanel, user, leaderboardGroupConfigPanel, stringMessages.leaderboardGroupConfiguration(), AdminConsoleFeatures.MANAGE_LEADERBOARD_GROUPS);
        regattaDisplayers.add(leaderboardGroupConfigPanel);

        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                stringMessages, /* showRaceDetails */true);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        addToTabPanel(tabPanel, user, leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), AdminConsoleFeatures.MANAGE_LEADERBOARDS);
        regattaDisplayers.add(leaderboardConfigPanel);

        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(tabPanel, user, resultImportUrlsManagementPanel, stringMessages.resultImportUrls(), AdminConsoleFeatures.MANAGE_RESULT_IMPORT_URLS);

        RemoteSailingServerInstancesManagementPanel sailingServerInstancesManagementPanel = new RemoteSailingServerInstancesManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(tabPanel, user, sailingServerInstancesManagementPanel, stringMessages.sailingServers(), AdminConsoleFeatures.MANAGE_SAILING_SERVER_INSTANCES);

        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
        // replicationPanel.ensureDebugId("ReplicationManagement");
        addToTabPanel(tabPanel, user, replicationPanel, stringMessages.replication(), AdminConsoleFeatures.MANAGE_REPLICATION);

        final MediaPanel mediaPanel = new MediaPanel(mediaService, this, stringMessages);
        addToTabPanel(tabPanel, user, mediaPanel, stringMessages.mediaPanel(), AdminConsoleFeatures.MANAGE_MEDIA);

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(stringMessages, sailingService,
                this, sailingEventManagementPanel, leaderboardGroupConfigPanel);
        addToTabPanel(tabPanel, user, masterDataImportPanel, stringMessages.masterDataImportPanel(), AdminConsoleFeatures.MANAGE_MASTERDATA_IMPORT);

        /*final DeviceConfigurationPanel deviceConfigurationAdminPanel = new DeviceConfigurationPanel(sailingService,
                stringMessages, this);
        addScrollableTab(tabPanel, deviceConfigurationAdminPanel, stringMessages.deviceConfiguration() + " (admin)");*/
        
        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                stringMessages, this);
        addToTabPanel(tabPanel, user, deviceConfigurationUserPanel, stringMessages.deviceConfiguration(), AdminConsoleFeatures.MANAGE_DEVICE_CONFIGURATION);

        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (leaderboardConfigPanel.isVisible()) {
                    leaderboardConfigPanel.loadLeaderboards();
                }
                if (mediaPanel.isVisible()) {
                    mediaPanel.onShow();
                }
                if (competitorPanel.isVisible()) {
                    competitorPanel.refreshCompetitorList();
                }
                if (raceLogTrackingEventManagementPanel.isVisible()) {
                    raceLogTrackingEventManagementPanel.loadLeaderboards();
                }
            }
        });
        fillRegattas();

        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(sailingService, this);
        sysinfoPanel.ensureDebugId("SystemInformation");
        dockPanel.addSouth(sysinfoPanel, 2.0);
        dockPanel.add(tabPanel);
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
    
//    private void addScrollableTab(TabLayoutPanel tabPanel, Widget widget, String tabTitle) {
//        ScrollPanel widgetScroller = new ScrollPanel();
//        widgetScroller.add(widget);
//        tabPanel.add(widgetScroller, tabTitle, false);
//    }

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
                new AsyncCallback<List<RegattaDTO>>() {
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
                }));
    }
}
