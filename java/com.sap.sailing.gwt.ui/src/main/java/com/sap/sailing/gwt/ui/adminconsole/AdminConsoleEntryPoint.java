package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
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
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;

public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    
    /**
     * The administration console's UI depends on the user's roles. When the roles change then so shall the display of tabs.
     * {@link AdminConsoleFeatures} list the roles to which they are made available. This map keeps track of the dependencies
     * and allows the UI to adjust to role changes.
     */
    private final LinkedHashMap<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, AdminConsoleFeatures> roleSpecificTabs = new LinkedHashMap<>();

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
                            final int selectedIndex = selectedTabLayoutPanel.getSelectedIndex();
                            if (selectedIndex >= 0) {
                                widgetAssociatedToVerticalTab = selectedTabLayoutPanel.getWidget(selectedIndex);
                            }
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
        setTabPanelSize(leaderboardTabPanel, "100%", "100%");
        leaderboardTabPanel.addSelectionHandler(tabSelectionHandler);
        leaderboardTabPanel.ensureDebugId("LeaderboardPanel");
        addToTabPanel(tabPanel, leaderboardTabPanel, stringMessages.leaderboards(), AdminConsoleFeatures.MANAGE_LEADERBOARDS);
        
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                stringMessages, /* showRaceDetails */true, this);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        addToTabPanel(leaderboardTabPanel, leaderboardConfigPanel, stringMessages.leaderboardConfiguration(), AdminConsoleFeatures.MANAGE_LEADERBOARDS);
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, this, this, stringMessages);
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        addToTabPanel(leaderboardTabPanel, leaderboardGroupConfigPanel, stringMessages.leaderboardGroups(), AdminConsoleFeatures.MANAGE_LEADERBOARD_GROUPS);
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);

        /* RACES */
        
        final TabLayoutPanel racesTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(racesTabPanel, "100%", "100%");
        racesTabPanel.addSelectionHandler(tabSelectionHandler);
        racesTabPanel.ensureDebugId("RacesPanel");
        addToTabPanel(tabPanel, racesTabPanel, stringMessages.races(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, stringMessages);
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        addToTabPanel(racesTabPanel, trackedRacesManagementPanel, stringMessages.trackedRaces(), AdminConsoleFeatures.SHOW_TRACKED_RACES);
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, stringMessages, this);
        addToTabPanel(racesTabPanel, competitorPanel, stringMessages.competitors(), AdminConsoleFeatures.MANAGE_ALL_COMPETITORS);

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this, stringMessages);
        addToTabPanel(racesTabPanel, raceCourseManagementPanel, stringMessages.courseLayout(), AdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, stringMessages);
        regattasDisplayers.add(windPanel);
        addToTabPanel(racesTabPanel, windPanel, stringMessages.wind(), AdminConsoleFeatures.MANAGE_WIND);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, this, mediaService, this, stringMessages);
        addToTabPanel(racesTabPanel, mediaPanel, stringMessages.mediaPanel(), AdminConsoleFeatures.MANAGE_MEDIA);

        /* CONNECTORS */
        
        final TabLayoutPanel connectorsTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(connectorsTabPanel, "100%", "100%");
        connectorsTabPanel.addSelectionHandler(tabSelectionHandler);
        connectorsTabPanel.ensureDebugId("TrackingProviderPanel");
        addToTabPanel(tabPanel, connectorsTabPanel, stringMessages.connectors(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);

        AbstractEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, stringMessages);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        addToTabPanel(connectorsTabPanel, tractracEventManagementPanel, stringMessages.tracTracEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, swissTimingReplayConnectorPanel, stringMessages.swissTimingArchiveConnector(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, swisstimingEventManagementPanel, stringMessages.swissTimingEvents(), AdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final RaceLogTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new RaceLogTrackingEventManagementPanel(
                sailingService, this, this, this, stringMessages);
        addToTabPanel(connectorsTabPanel, raceLogTrackingEventManagementPanel, stringMessages.raceLogTracking(), AdminConsoleFeatures.MANAGE_RACELOG_TRACKING);
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, stringMessages);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        addToTabPanel(connectorsTabPanel, igtimiAccountsPanel, stringMessages.igtimiAccounts(), AdminConsoleFeatures.MANAGE_IGTIMI_ACCOUNTS);
        
        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(connectorsTabPanel, resultImportUrlsManagementPanel, stringMessages.resultImportUrls(), AdminConsoleFeatures.MANAGE_RESULT_IMPORT_URLS);

        /* ADVANCED */
        
        final TabLayoutPanel advancedTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        setTabPanelSize(advancedTabPanel, "100%", "100%");
        advancedTabPanel.addSelectionHandler(tabSelectionHandler);
        advancedTabPanel.ensureDebugId("AdvancedPanel");
        addToTabPanel(tabPanel, advancedTabPanel, stringMessages.advanced(), AdminConsoleFeatures.MANAGE_REPLICATION);

        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, stringMessages);
        addToTabPanel(advancedTabPanel, replicationPanel, stringMessages.replication(), AdminConsoleFeatures.MANAGE_REPLICATION);

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(stringMessages, sailingService,
                this, eventManagementPanel, this, this);
        addToTabPanel(advancedTabPanel, masterDataImportPanel, stringMessages.masterDataImportPanel(), AdminConsoleFeatures.MANAGE_MASTERDATA_IMPORT);

        RemoteSailingServerInstancesManagementPanel sailingServerInstancesManagementPanel = new RemoteSailingServerInstancesManagementPanel(sailingService, this, stringMessages);
        addToTabPanel(advancedTabPanel, sailingServerInstancesManagementPanel, stringMessages.sailingServers(), AdminConsoleFeatures.MANAGE_SAILING_SERVER_INSTANCES);

        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                stringMessages, this);
        addToTabPanel(advancedTabPanel, deviceConfigurationUserPanel, stringMessages.deviceConfiguration(), AdminConsoleFeatures.MANAGE_DEVICE_CONFIGURATION);
        updateTabDisplayForCurrentUser(getUserService().getCurrentUser());
        if (tabPanel.getWidgetCount() > 0) {
            tabPanel.selectTab(0);
        }
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();

        final DockPanel informationPanel = new DockPanel();
        informationPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    informationPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
                }
            }
        });
        informationPanel.setSize("100%", "95%");
        informationPanel.setSpacing(10);
        informationPanel.add(new LoginPanel(SecurityStylesheetResources.INSTANCE.css(), getUserService()), DockPanel.WEST);
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

    /**
     * Sets the sizie of the tab panel when the tab panel is attached to the DOM
     */
    private void setTabPanelSize(final TabLayoutPanel advancedTabPanel, final String width, final String height) {
        advancedTabPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("width", width);
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("height", height);
            }
        });
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

    private void addToTabPanel(final VerticalTabLayoutPanel tabPanel, Widget panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
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
    
    private void addToTabPanel(final TabLayoutPanel tabPanel, Widget panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
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
            AdminConsoleFeatures feature) {
        roleSpecificTabs.put(new Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>(tabPanel, wrapInScrollPanel(panelToAdd), tabTitle), feature);
    }
    
    /**
     * After initialization or whenever the user changes, the tab display is adjusted based on which roles are required
     * to see which tabs. See {@link #roleSpecificTabs}.
     */
    private void updateTabDisplayForCurrentUser(UserDTO user) {
        for (Map.Entry<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, AdminConsoleFeatures> e : roleSpecificTabs.entrySet()) {
            final Widget panelToAdd = e.getKey().getB();
            if (user != null && isUserInRole(e.getValue().getEnabledRoles())) {
                e.getKey().getA().add(panelToAdd, e.getKey().getC(), /* asHtml */ false);
            } else {
                e.getKey().getA().remove(panelToAdd);
            }
        }
    }
    
    private boolean isUserInRole(UserRoles[] roles) {
        boolean result = true;
        UserDTO user = getUserService().getCurrentUser();
        for (UserRoles enabledRole : roles) {
            if (user.hasRole(enabledRole.name())) {
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
