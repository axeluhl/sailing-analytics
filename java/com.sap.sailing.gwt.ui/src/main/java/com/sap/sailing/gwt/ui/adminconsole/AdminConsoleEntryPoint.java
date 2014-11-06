package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
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
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.RefreshableAdminConsolePanel;
import com.sap.sse.gwt.adminconsole.SystemInformationPanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;

public class AdminConsoleEntryPoint extends AbstractSailingEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    
    /**
     * The administration console's UI depends on the user's roles. When the roles change then so shall the display of tabs.
     * {@link SailingAdminConsoleFeatures} list the roles to which they are made available. This map keeps track of the dependencies
     * and allows the UI to adjust to role changes.
     */
    private final LinkedHashMap<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, SailingAdminConsoleFeatures> roleSpecificTabs = new LinkedHashMap<>();

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                updateTabDisplayForCurrentUser(user);
            }
        });
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
        createUI();
    }
     
    private void createUI() {
        AdminConsolePanel panel = new AdminConsolePanel(getUserService(), sailingService, persistentAlertLabel,
                getStringMessages().releaseNotes(), "/release_notes_admin.html", /* error reporter */ this);
        BetterDateTimeBox.initialize();
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();

        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService, this, getStringMessages());
        panel.addToVerticalTabPanel(new RefreshableAdminConsolePanel() {
            private final EventManagementPanel widget = new EventManagementPanel(sailingService, AdminConsoleEntryPoint.this, getStringMessages());
            @Override
            public void refreshAfterBecomingVisible() {
                widget.fillEvents();
                fillLeaderboardGroups();
            }
            
            @Override
            public Widget getWidget() {
                return widget;
            }
        }, getStringMessages().events(), SailingAdminConsoleFeatures.MANAGE_EVENTS);
        leaderboardGroupsDisplayers.add(eventManagementPanel);

        RegattaStructureManagementPanel regattaManagementPanel = new RegattaStructureManagementPanel(
                sailingService, this, getStringMessages(), this);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        addToTabPanel(tabPanel, regattaManagementPanel, getStringMessages().regattas(), SailingAdminConsoleFeatures.MANAGE_REGATTAS);
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        
        final TabLayoutPanel leaderboardTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(leaderboardTabPanel, "100%", "100%");
        leaderboardTabPanel.addSelectionHandler(tabSelectionHandler);
        leaderboardTabPanel.ensureDebugId("LeaderboardPanel");
        addToTabPanel(tabPanel, leaderboardTabPanel, getStringMessages().leaderboards(), SailingAdminConsoleFeatures.MANAGE_LEADERBOARDS);
        
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                getStringMessages(), /* showRaceDetails */true, this);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        addToTabPanel(leaderboardTabPanel, leaderboardConfigPanel, getStringMessages().leaderboardConfiguration(), SailingAdminConsoleFeatures.MANAGE_LEADERBOARDS);
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, this, this, getStringMessages());
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        addToTabPanel(leaderboardTabPanel, leaderboardGroupConfigPanel, getStringMessages().leaderboardGroups(), SailingAdminConsoleFeatures.MANAGE_LEADERBOARD_GROUPS);
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);

        /* RACES */
        
        final TabLayoutPanel racesTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(racesTabPanel, "100%", "100%");
        racesTabPanel.addSelectionHandler(tabSelectionHandler);
        racesTabPanel.ensureDebugId("RacesPanel");
        addToTabPanel(tabPanel, racesTabPanel, getStringMessages().races(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        addToTabPanel(racesTabPanel, trackedRacesManagementPanel, getStringMessages().trackedRaces(), SailingAdminConsoleFeatures.SHOW_TRACKED_RACES);
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, getStringMessages(), this);
        addToTabPanel(racesTabPanel, competitorPanel, getStringMessages().competitors(), SailingAdminConsoleFeatures.MANAGE_ALL_COMPETITORS);

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this, getStringMessages());
        addToTabPanel(racesTabPanel, raceCourseManagementPanel, getStringMessages().courseLayout(), SailingAdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, getStringMessages());
        regattasDisplayers.add(windPanel);
        addToTabPanel(racesTabPanel, windPanel, getStringMessages().wind(), SailingAdminConsoleFeatures.MANAGE_WIND);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, this, mediaService, this, getStringMessages());
        addToTabPanel(racesTabPanel, mediaPanel, getStringMessages().mediaPanel(), SailingAdminConsoleFeatures.MANAGE_MEDIA);

        /* CONNECTORS */
        
        final TabLayoutPanel connectorsTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(connectorsTabPanel, "100%", "100%");
        connectorsTabPanel.addSelectionHandler(tabSelectionHandler);
        connectorsTabPanel.ensureDebugId("TrackingProviderPanel");
        addToTabPanel(tabPanel, connectorsTabPanel, getStringMessages().connectors(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);

        AbstractEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, getStringMessages());
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        addToTabPanel(connectorsTabPanel, tractracEventManagementPanel, getStringMessages().tracTracEvents(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, getStringMessages());
        addToTabPanel(connectorsTabPanel, swissTimingReplayConnectorPanel, getStringMessages().swissTimingArchiveConnector(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, getStringMessages());
        addToTabPanel(connectorsTabPanel, swisstimingEventManagementPanel, getStringMessages().swissTimingEvents(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final RaceLogTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new RaceLogTrackingEventManagementPanel(
                sailingService, this, this, this, getStringMessages());
        addToTabPanel(connectorsTabPanel, raceLogTrackingEventManagementPanel, getStringMessages().raceLogTracking(), SailingAdminConsoleFeatures.MANAGE_RACELOG_TRACKING);
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, getStringMessages());
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        addToTabPanel(connectorsTabPanel, igtimiAccountsPanel, getStringMessages().igtimiAccounts(), SailingAdminConsoleFeatures.MANAGE_IGTIMI_ACCOUNTS);
        
        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(sailingService, this, getStringMessages());
        addToTabPanel(connectorsTabPanel, resultImportUrlsManagementPanel, getStringMessages().resultImportUrls(), SailingAdminConsoleFeatures.MANAGE_RESULT_IMPORT_URLS);

        /* ADVANCED */
        
        final TabLayoutPanel advancedTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(advancedTabPanel, "100%", "100%");
        advancedTabPanel.addSelectionHandler(tabSelectionHandler);
        advancedTabPanel.ensureDebugId("AdvancedPanel");
        addToTabPanel(tabPanel, advancedTabPanel, getStringMessages().advanced(), SailingAdminConsoleFeatures.MANAGE_REPLICATION);

        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, getStringMessages());
        addToTabPanel(advancedTabPanel, replicationPanel, getStringMessages().replication(), SailingAdminConsoleFeatures.MANAGE_REPLICATION);

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(getStringMessages(), sailingService,
                this, eventManagementPanel, this, this);
        addToTabPanel(advancedTabPanel, masterDataImportPanel, getStringMessages().masterDataImportPanel(), SailingAdminConsoleFeatures.MANAGE_MASTERDATA_IMPORT);

        RemoteSailingServerInstancesManagementPanel sailingServerInstancesManagementPanel = new RemoteSailingServerInstancesManagementPanel(sailingService, this, getStringMessages());
        addToTabPanel(advancedTabPanel, sailingServerInstancesManagementPanel, getStringMessages().sailingServers(), SailingAdminConsoleFeatures.MANAGE_SAILING_SERVER_INSTANCES);

        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                getStringMessages(), this);
        addToTabPanel(advancedTabPanel, deviceConfigurationUserPanel, getStringMessages().deviceConfiguration(), SailingAdminConsoleFeatures.MANAGE_DEVICE_CONFIGURATION);
        updateTabDisplayForCurrentUser(getUserService().getCurrentUser());
        if (tabPanel.getWidgetCount() > 0) {
            tabPanel.selectTab(0);
        }
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();

        final DockPanel informationPanel = new DockPanel();
        informationPanel.setSize("100%", "95%");
        informationPanel.setSpacing(10);
        informationPanel.add(new LoginPanel(SecurityStylesheetResources.INSTANCE.css(), getUserService()), DockPanel.WEST);
        informationPanel.add(persistentAlertLabel, DockPanel.CENTER);

        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(sailingService, this);
        sysinfoPanel.ensureDebugId("SystemInformation");

        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(getStringMessages().releaseNotes()).toSafeHtml(), "/release_notes_admin.html");
        sysinfoPanel.add(releaseNotesLink);
        informationPanel.add(sysinfoPanel, DockPanel.EAST);
        informationPanel.setCellHorizontalAlignment(sysinfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);

        dockPanel.addSouth(informationPanel, 2.5);
        dockPanel.add(tabPanel);
        rootPanel.add(dockPanel);
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
    
    private static interface VerticalOrHorizontalTabLayoutPanel {
        void add(Widget child, String text, boolean asHtml);
        void remove(Widget child);
    }

    private void addToTabPanel(final VerticalTabLayoutPanel tabPanel, Widget panelToAdd, String tabTitle, SailingAdminConsoleFeatures feature) {
        VerticalOrHorizontalTabLayoutPanel wrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                tabPanel.add(child, text, asHtml);
                tabPanel.forceLayout();
            }
            @Override
            public void remove(Widget child) {
                tabPanel.remove(child);
            }
        };
        addToTabPanel(wrapper, panelToAdd, tabTitle, feature);
    }

    private ScrollPanel wrapInScrollPanel(Widget panelToAdd) {
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(panelToAdd);
        panelToAdd.setSize("100%", "100%");
        return scrollPanel;
    }
    
    private void addToTabPanel(final TabLayoutPanel tabPanel, Widget panelToAdd, String tabTitle, SailingAdminConsoleFeatures feature) {
        VerticalOrHorizontalTabLayoutPanel wrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                tabPanel.add(child, text, asHtml);
                tabPanel.forceLayout();
            }
            @Override
            public void remove(Widget child) {
                tabPanel.remove(child);
            }
        };
        addToTabPanel(wrapper, panelToAdd, tabTitle, feature);
    }

    private void addToTabPanel(VerticalOrHorizontalTabLayoutPanel tabPanel, Widget panelToAdd, String tabTitle,
            SailingAdminConsoleFeatures feature) {
        roleSpecificTabs.put(new Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>(tabPanel, wrapInScrollPanel(panelToAdd), tabTitle), feature);
    }
    
    /**
     * After initialization or whenever the user changes, the tab display is adjusted based on which roles are required
     * to see which tabs. See {@link #roleSpecificTabs}.
     */
    private void updateTabDisplayForCurrentUser(UserDTO user) {
        for (Map.Entry<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, SailingAdminConsoleFeatures> e : roleSpecificTabs.entrySet()) {
            final Widget panelToAdd = e.getKey().getB();
            if (user != null && isUserInRole(e.getValue().getEnabledRoles())) {
                e.getKey().getA().add(panelToAdd, e.getKey().getC(), /* asHtml */ false);
            } else {
                e.getKey().getA().remove(panelToAdd);
            }
        }
    }
    
    private boolean isUserInRole(UserRoles[] roles) {
        boolean result = false;
        UserDTO user = getUserService().getCurrentUser();
        for (UserRoles enabledRole : roles) {
            if (user.hasRole(enabledRole.getRolename())) {
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
