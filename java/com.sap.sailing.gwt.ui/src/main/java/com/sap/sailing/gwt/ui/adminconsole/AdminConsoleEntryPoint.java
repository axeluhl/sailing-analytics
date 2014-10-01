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
import com.google.gwt.user.client.ui.RootPanel;
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
import com.sap.sailing.gwt.ui.client.shared.panels.SystemInformationPanel;
import com.sap.sailing.gwt.ui.client.shared.panels.UserStatusPanel;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);

        createUI();
    }
     
    private void createUI() {
        /* Generic selection handler that forwards selected tabs to a refresher that ensures
         * that data gets reloaded. If you add a new tab then make sure to have a look at
         * #refreshDataFor(Widget widget) to ensure that upon selection your tab gets the 
         * data refreshed.
         */
        final SelectionHandler<Integer> tabSelectionHandler = new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                Object source = event.getSource();
                if (source != null) {
                    if (source instanceof TabLayoutPanel) {
                        final TabLayoutPanel tabPanel = ((TabLayoutPanel)source);
                        final Widget selectedPanel = tabPanel.getWidget(event.getSelectedItem());
                        refreshDataFor(selectedPanel);
                    } else if (source instanceof VerticalTabLayoutPanel) {
                        final VerticalTabLayoutPanel verticalTabLayoutPanel = (VerticalTabLayoutPanel)source;
                        Widget widgetAssociatedToVerticalTab = verticalTabLayoutPanel.getWidget(verticalTabLayoutPanel.getSelectedIndex());
                        if (widgetAssociatedToVerticalTab instanceof TabLayoutPanel) {
                            TabLayoutPanel selectedTabLayoutPanel = (TabLayoutPanel)widgetAssociatedToVerticalTab;
                            widgetAssociatedToVerticalTab = selectedTabLayoutPanel.getWidget(selectedTabLayoutPanel.getSelectedIndex());
                        }
                        refreshDataFor(widgetAssociatedToVerticalTab);
                    }
                }
            }
        };
    
        BetterDateTimeBox.initialize();
        
        final VerticalTabLayoutPanel tabPanel = new VerticalTabLayoutPanel(2.5, Unit.EM);
        tabPanel.addSelectionHandler(tabSelectionHandler);
        tabPanel.ensureDebugId("AdministrationTabs");
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();

        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(tabPanel, eventManagementPanel, stringMessages.events(), AdminConsoleFeatures.MANAGE_EVENTS);
        leaderboardGroupsDisplayers.add(eventManagementPanel);

        RegattaStructureManagementPanel regattaManagementPanel = new RegattaStructureManagementPanel(
                sailingService, this, stringMessages, this);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        addToTabPanel(tabPanel, regattaManagementPanel, stringMessages.regattas(), AdminConsoleFeatures.MANAGE_REGATTAS);
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        
        final TabLayoutPanel leaderboardTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        leaderboardTabPanel.addSelectionHandler(tabSelectionHandler);
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

        /* RACES */
        
        final TabLayoutPanel racesTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        racesTabPanel.addSelectionHandler(tabSelectionHandler);
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
        addToTabPanel(racesTabPanel, user, raceCourseManagementPanel, stringMessages.courseLayout(), AdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        regattasDisplayers.add(windPanel);
        addToTabPanel(racesTabPanel, user, windPanel, stringMessages.wind(), AdminConsoleFeatures.MANAGE_WIND);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, this, mediaService, this, stringMessages);
        addToTabPanel(racesTabPanel, user, mediaPanel, stringMessages.mediaPanel(), AdminConsoleFeatures.MANAGE_MEDIA);

        /* CONNECTORS */
        
        final TabLayoutPanel connectorsTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        connectorsTabPanel.addSelectionHandler(tabSelectionHandler);
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

        /* ADVANCED */
        
        final TabLayoutPanel advancedTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        advancedTabPanel.addSelectionHandler(tabSelectionHandler);
        advancedTabPanel.ensureDebugId("AdvancedPanel");
        tabPanel.add(advancedTabPanel, "Advanced");

        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
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
        rootPanel.add(new LoginPanel());
    }
    
    private void refreshDataFor(Widget widget) {
        Widget target = widget;
        if (widget != null) {
            if (widget instanceof ScrollPanel) {
                target = ((ScrollPanel)widget).getWidget();
            }
            if (target instanceof EventManagementPanel) {
                ((EventManagementPanel)target).fillEvents();
                fillLeaderboardGroups();
            } else if (target instanceof LeaderboardConfigPanel) {
                fillLeaderboards();
            } else if (target instanceof LeaderboardGroupConfigPanel) {
                fillLeaderboardGroups();
                fillLeaderboards();
            } else if (target instanceof RegattaStructureManagementPanel) {
                fillRegattas();
            } else if (target instanceof CompetitorPanel) {
                ((CompetitorPanel)target).refreshCompetitorList();
            } else if (target instanceof RaceLogTrackingEventManagementPanel) {
                fillLeaderboards();
            } else if (target instanceof MediaPanel) {
                ((MediaPanel)target).onShow();
            }
        }
    }

    private void addToTabPanel(VerticalTabLayoutPanel tabPanel, UserDTO user, Panel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        if(user != null && isUserInRole(user, feature.getEnabledRoles())) {
            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.add(panelToAdd);
            panelToAdd.setSize("90%", "90%");
            tabPanel.add(scrollPanel, tabTitle, false);
        }
    }
    
    private void addToTabPanel(TabLayoutPanel tabPanel, Panel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        if(/*TODO [S056866] user != null &&*/ isUserInRole(feature.getEnabledRoles())) {
            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.add(panelToAdd);
            panelToAdd.setSize("90%", "90%");
            tabPanel.add(scrollPanel, tabTitle, false);
        }
    }
    
    private boolean isUserInRole(/*TODO [S056866] UserDTO user,*/ UserRoles[] roles) {
        boolean result = true;
//        for(UserRoles enabledRole: roles) {
//            if (user.roles.contains(enabledRole.name())) {
//                result = true;
//                break;
//            }
//        }
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
