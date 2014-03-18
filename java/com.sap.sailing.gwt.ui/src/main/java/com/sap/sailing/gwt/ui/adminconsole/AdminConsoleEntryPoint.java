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
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
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
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher {
    private Set<RegattaDisplayer> regattaDisplayers;

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);
        registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);

        BetterDateTimeBox.initialize();
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        rootPanel.add(dockPanel);

        DockPanel topInformationPanel = new DockPanel();
        topInformationPanel.setSize("100%", "95%");
        UserStatusPanel userStatusPanel = new UserStatusPanel(userManagementService, this);
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
        sailingEventManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, sailingEventManagementPanel, stringMessages.events());

        RegattaStructureManagementPanel eventStructureManagementPanel = new RegattaStructureManagementPanel(
                sailingService, this, stringMessages, this);
        eventStructureManagementPanel.ensureDebugId("RegattaStructureManagement");
        eventStructureManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, eventStructureManagementPanel, stringMessages.regattas());
        regattaDisplayers.add(eventStructureManagementPanel);

        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, stringMessages);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        tractracEventManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, tractracEventManagementPanel, stringMessages.tracTracEvents());
        regattaDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, stringMessages);
        swissTimingReplayConnectorPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, swissTimingReplayConnectorPanel, stringMessages.swissTimingArchiveConnector());
        regattaDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, stringMessages);
        // swisstimingEventManagementPanel.ensureDebugId("SwissTimingEventManagement");
        swisstimingEventManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, swisstimingEventManagementPanel, stringMessages.swissTimingEvents());
        regattaDisplayers.add(swisstimingEventManagementPanel);

        CreateSwissTimingRacePanel createSwissTimingRacePanel = new CreateSwissTimingRacePanel(sailingService, this,
                stringMessages);
        // createSwissTimingRacePanel.ensureDebugId("CreateSwissTimingRace");
        createSwissTimingRacePanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, createSwissTimingRacePanel, stringMessages.createSwissTimingRace());

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, stringMessages);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        igtimiAccountsPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, igtimiAccountsPanel, stringMessages.igtimiAccounts());

        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, stringMessages);
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        trackedRacesManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, trackedRacesManagementPanel, stringMessages.trackedRaces());
        regattaDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, stringMessages, this);
        // trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        trackedRacesManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, competitorPanel, stringMessages.competitors());

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this,
                stringMessages);
        // raceCourseManagementPanel.ensureDebugId("RaceCourseManagement");
        raceCourseManagementPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, raceCourseManagementPanel, stringMessages.courseLayout());
        regattaDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        // windPanel.ensureDebugId("WindManagement");
        regattaDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, windPanel, stringMessages.wind());

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, stringMessages);
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        leaderboardGroupConfigPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, leaderboardGroupConfigPanel, stringMessages.leaderboardGroupConfiguration());
        regattaDisplayers.add(leaderboardGroupConfigPanel);

        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                stringMessages,
                /* showRaceDetails */true);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        leaderboardConfigPanel.setSize("90%", "90%");
        addScrollableTab(tabPanel, leaderboardConfigPanel, stringMessages.leaderboardConfiguration());
        regattaDisplayers.add(leaderboardConfigPanel);

        addScrollableTab(tabPanel, new ResultImportUrlsManagementPanel(sailingService, this, stringMessages),
                stringMessages.resultImportUrls());

        ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
        // replicationPanel.ensureDebugId("ReplicationManagement");
        addScrollableTab(tabPanel, replicationPanel, stringMessages.replication());
        final MediaPanel mediaPanel = new MediaPanel(mediaService, this, stringMessages);
        addScrollableTab(tabPanel, mediaPanel, stringMessages.mediaPanel());

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(stringMessages, sailingService,
                this, sailingEventManagementPanel, leaderboardGroupConfigPanel);
        addScrollableTab(tabPanel, masterDataImportPanel, stringMessages.masterDataImportPanel());

        /*final DeviceConfigurationPanel deviceConfigurationAdminPanel = new DeviceConfigurationPanel(sailingService,
                stringMessages, this);
        addScrollableTab(tabPanel, deviceConfigurationAdminPanel, stringMessages.deviceConfiguration() + " (admin)");*/
        
        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                stringMessages, this);
        addScrollableTab(tabPanel, deviceConfigurationUserPanel, stringMessages.deviceConfiguration());

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
            }
        });
        fillRegattas();

        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(sailingService, this);
        sysinfoPanel.ensureDebugId("SystemInformation");
        dockPanel.addSouth(sysinfoPanel, 2.0);
        dockPanel.add(tabPanel);
    }

    private void addScrollableTab(TabLayoutPanel tabPanel, Widget widget, String tabTitle) {
        ScrollPanel widgetScroller = new ScrollPanel();
        widgetScroller.add(widget);
        tabPanel.add(widgetScroller, tabTitle, false);
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
