package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.security.SecuredDomainType.TrackedRaceActions;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.adminconsole.BoatPanel;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorPanel;
import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationPanel;
import com.sap.sailing.gwt.ui.adminconsole.EventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.ExpeditionDeviceConfigurationsPanel;
import com.sap.sailing.gwt.ui.adminconsole.IgtimiAccountsPanel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupConfigPanel;
import com.sap.sailing.gwt.ui.adminconsole.LocalServerManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.MediaPanel;
import com.sap.sailing.gwt.ui.adminconsole.RaceCourseManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.RegattaManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.RemoteServerInstancesManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.ResultImportUrlsListComposite;
import com.sap.sailing.gwt.ui.adminconsole.SmartphoneTrackingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.StructureImportManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingReplayConnectorPanel;
import com.sap.sailing.gwt.ui.adminconsole.TracTracEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.TrackedRacesManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.WindPanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.CourseTemplatePanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkPropertiesPanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkRolePanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkTemplatePanel;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.DefaultRefreshableAdminConsolePanel;
import com.sap.sse.gwt.adminconsole.ReplicationPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.controls.filestorage.FileStoragePanel;
import com.sap.sse.gwt.client.panels.HorizontalTabLayoutPanel;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.RoleDefinitionsPanel;
import com.sap.sse.security.ui.client.component.UserGroupManagementPanel;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;

public class AdminConsoleViewImpl extends Composite implements AdminConsoleView {

    private final AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
    
    private HeaderPanel headerPanel;
    
    private Presenter presenter;
    
    private StringMessages stringMessages;
    
    private UserService userService;
    
    private SailingServiceWriteAsync sailingService;

    private MediaServiceWriteAsync mediaServiceWrite;
    
    private ErrorReporter errorReporter;
    
    private AdminConsolePanel adminConsolePanel;
    
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;
    
    private String verticalTabName;
    private String horizontalTabName;
    
    public AdminConsoleViewImpl() {
        headerPanel = new HeaderPanel();
        initWidget(headerPanel);
    }
    
    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
        
        this.regattasDisplayers = presenter.getRegattasDisplayers();
        this.leaderboardsDisplayers = presenter.getLeaderboardsDisplayer();
        this.leaderboardGroupsDisplayers = presenter.getLeaderboardGroupsDisplayer();
        
        this.userService = presenter.getUserService();
        this.sailingService = presenter.getSailingService();
        this.mediaServiceWrite = presenter.getMediaServiceWrite();
        
        this.stringMessages = StringMessages.INSTANCE;
        this.errorReporter = presenter.getErrorReporter();

    }
    
    private StringMessages getStringMessages() {
        return stringMessages;
    }
    
    @Override
    public HeaderPanel createUI(final ServerInfoDTO serverInfo) {
        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().administration());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(userService, header.getAuthenticationMenuView());
        AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
        authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
          
            @Override
            public Widget get() {
                return createAdminConsolePanel(serverInfo);
            }
        });
        headerPanel.setHeaderWidget(header);
        headerPanel.setContentWidget(authorizedContentDecorator);
        
        return headerPanel;
    }
    
    @Override
    public void selectTabByNames(final String verticalTabName, final String horizontalTabName) {
        this.verticalTabName = verticalTabName;
        this.horizontalTabName = horizontalTabName;
    }
    
    @Override
    public void goToTabByNames(final String verticalTabName, final String horizontalTabName) {
        this.verticalTabName = verticalTabName;
        this.horizontalTabName = horizontalTabName;
        adminConsolePanel.selectTabByNames(verticalTabName, horizontalTabName, new HashMap<>());
    }
    
    private AdminConsolePanel createAdminConsolePanel(final ServerInfoDTO serverInfo) {
        adminConsolePanel = new AdminConsolePanel(userService, 
                serverInfo, getStringMessages().releaseNotes(), "/release_notes_admin.html", errorReporter,
                SecurityStylesheetResources.INSTANCE.css(), stringMessages);
        adminConsolePanel.addStyleName("adminConsolePanel");

        
        /* EVENTS */
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService,
                userService, errorReporter, presenter, getStringMessages(), adminConsolePanel);
        eventManagementPanel.ensureDebugId("EventManagement");
        adminConsolePanel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                presenter.fillLeaderboardGroups();
            }
        }, getStringMessages().events(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));
        leaderboardGroupsDisplayers.add(eventManagementPanel);
        
        /* REGATTAS */
        
        //SailingServiceWriteAsync sailingServiceWrite, UserService userService,
        //ErrorReporter errorReporter, StringMessages stringMessages, RegattaRefresher regattaRefresher,
        //EventsRefresher eventsRefresher
        RegattaManagementPanel regattaManagementPanel = new RegattaManagementPanel(
                sailingService, userService, errorReporter, getStringMessages(), presenter, eventManagementPanel);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        adminConsolePanel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<RegattaManagementPanel>(regattaManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillRegattas();
            }
        }, getStringMessages().regattas(), SecuredDomainType.REGATTA.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        final HorizontalTabLayoutPanel leaderboardTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().leaderboards(), "LeaderboardPanel");
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, userService, presenter, errorReporter,
                getStringMessages(), /* showRaceDetails */true, presenter);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        adminConsolePanel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardConfigPanel>(leaderboardConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillLeaderboards();
            }
        }, getStringMessages().leaderboards(), SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.MUTATION_ACTIONS));     
        
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(
                sailingService, userService, presenter, presenter, presenter, errorReporter, getStringMessages());
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        adminConsolePanel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardGroupConfigPanel>(leaderboardGroupConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillLeaderboards();
                presenter.fillLeaderboardGroups();
            }

            @Override
            public void setupWidgetByParams(Map<String, String> params) {
                refreshAfterBecomingVisible(); //Refresh to sure that actual data is provided
                presenter.setupLeaderboardGroups(leaderboardGroupConfigPanel, params);
            }
        }, getStringMessages().leaderboardGroups(), SecuredDomainType.LEADERBOARD_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);
        
        /* RACES */
        final HorizontalTabLayoutPanel racesTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().trackedRaces(), "RacesPanel");

        final TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().trackedRaces(),
                SecuredDomainType.TRACKED_RACE.getPermission(TrackedRaceActions.MUTATION_ACTIONS));
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, userService, getStringMessages(), errorReporter);
        competitorPanel.ensureDebugId("CompetitorPanel");
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors(),
                SecuredDomainType.COMPETITOR.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        final BoatPanel boatPanel = new BoatPanel(sailingService, userService, getStringMessages(), errorReporter);
        boatPanel.ensureDebugId("BoatPanel");
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<BoatPanel>(boatPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshBoatList();
            }
        }, getStringMessages().boats(),
                SecuredDomainType.BOAT.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, errorReporter,
                presenter, getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().courseLayout(),
                SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        final WindPanel windPanel = new WindPanel(sailingService, userService, asyncActionsExecutor, errorReporter,
                presenter, getStringMessages());
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<WindPanel>(windPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillRegattas();
            }
        }, getStringMessages().wind(), SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));
        regattasDisplayers.add(windPanel);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, presenter, mediaServiceWrite, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel(),
                SecuredDomainType.MEDIA_TRACK.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* RACE COMMITTEE APP */
        final HorizontalTabLayoutPanel raceCommitteeTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().raceCommitteeApp(), "RaceCommiteeAppPanel");
        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationPanel(
                sailingService, userService, getStringMessages(), errorReporter);
        adminConsolePanel.addToTabPanel(raceCommitteeTabPanel,
                new DefaultRefreshableAdminConsolePanel<DeviceConfigurationPanel>(deviceConfigurationUserPanel),
                getStringMessages().deviceConfiguration(),
                SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION.getPermission(DefaultActions.MUTATION_ACTIONS));
        
        /* CONNECTORS */
        final HorizontalTabLayoutPanel connectorsTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().connectors(), "TrackingProviderPanel");
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages(), tableResources);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<TracTracEventManagementPanel>(tractracEventManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        tractracEventManagementPanel.refreshTracTracConnectors();
                    }
                },
                getStringMessages().tracTracEvents(),
                SecuredDomainType.TRACTRAC_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(tractracEventManagementPanel);
        
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(swissTimingReplayConnectorPanel),
                getStringMessages().swissTimingArchiveConnector(),
                SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swisstimingEventManagementPanel),
                getStringMessages().swissTimingEvents(),
                SecuredDomainType.SWISS_TIMING_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final SmartphoneTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new SmartphoneTrackingEventManagementPanel(
                sailingService, userService, presenter, presenter, errorReporter, getStringMessages());
        raceLogTrackingEventManagementPanel.ensureDebugId("SmartphoneTrackingPanel");
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<SmartphoneTrackingEventManagementPanel>(
                        raceLogTrackingEventManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillLeaderboards();
                    }
                }, getStringMessages().smartphoneTracking(),
                SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.UPDATE, DefaultActions.DELETE));
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, errorReporter,
                getStringMessages(), userService);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<IgtimiAccountsPanel>(igtimiAccountsPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        igtimiAccountsPanel.refresh();
                    }
                },
                getStringMessages().igtimiAccounts(),
                SecuredDomainType.IGTIMI_ACCOUNT.getPermission(DefaultActions.values()));
        ExpeditionDeviceConfigurationsPanel expeditionDeviceConfigurationsPanel = new ExpeditionDeviceConfigurationsPanel(sailingService, errorReporter, getStringMessages(), userService);
        expeditionDeviceConfigurationsPanel.ensureDebugId("ExpeditionDeviceConfigurations");
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ExpeditionDeviceConfigurationsPanel>(expeditionDeviceConfigurationsPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                expeditionDeviceConfigurationsPanel.refresh();
            }
        }, getStringMessages().expeditionDeviceConfigurations(),
                SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getPermission(DefaultActions.values()));

        ResultImportUrlsListComposite resultUrlsListComposite = new ResultImportUrlsListComposite(sailingService,
                userService, errorReporter, getStringMessages());
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<ResultImportUrlsListComposite>(resultUrlsListComposite),
                getStringMessages().resultImportUrls(),
                SecuredDomainType.RESULT_IMPORT_URL.getPermission(DefaultActions.values()));

        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(
                sailingService, userService, errorReporter, getStringMessages(), presenter, eventManagementPanel);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(structureImportUrlsManagementPanel),
                getStringMessages().manage2Sail() + " " + getStringMessages().regattaStructureImport(),
                SecuredDomainType.REGATTA.getPermission(DefaultActions.CREATE));

        /* ADVANCED */
        final HorizontalTabLayoutPanel advancedTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().advanced(),
                "AdvancedTab");
        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, userService, errorReporter,
                getStringMessages());
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<ReplicationPanel>(replicationPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                replicationPanel.updateReplicaList();
            }
                }, getStringMessages().replication(),
                () -> userService.hasAnyServerPermission(ServerActions.REPLICATE, ServerActions.START_REPLICATION,
                        ServerActions.READ_REPLICATOR));
        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(getStringMessages(), sailingService,
                presenter, eventManagementPanel, presenter, presenter, mediaPanel);
        masterDataImportPanel.ensureDebugId("MasterDataImport");
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<MasterDataImportPanel>(masterDataImportPanel),
                getStringMessages().masterDataImportPanel(), SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CAN_IMPORT_MASTERDATA, serverInfo));
        RemoteServerInstancesManagementPanel remoteServerInstancesManagementPanel = new RemoteServerInstancesManagementPanel(sailingService, userService,
                errorReporter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<RemoteServerInstancesManagementPanel>(remoteServerInstancesManagementPanel),
                getStringMessages().remoteServerInstances(),
                SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_REMOTE_INSTANCES, serverInfo));
        final LocalServerManagementPanel localServerInstancesManagementPanel = new LocalServerManagementPanel(
                sailingService, userService, errorReporter, getStringMessages());
        localServerInstancesManagementPanel.ensureDebugId("LocalServer");
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<LocalServerManagementPanel>(
                        localServerInstancesManagementPanel) {
                            @Override
                            public void refreshAfterBecomingVisible() {
                                localServerInstancesManagementPanel.refreshServerConfiguration();
                            }
        }, getStringMessages().localServer(),
                // We explicitly use a different permission check here.
                // Most panels show a list of domain objects which means we check if the user has permissions for any
                // potentially existing object to decide about the visibility.
                // The local server tab is about the specific server object. This check needs to contain the ownership
                // information to work as intended.
                () -> userService.hasAnyServerPermission(ServerActions.CONFIGURE_LOCAL_SERVER,
                        DefaultActions.CHANGE_OWNERSHIP, DefaultActions.CHANGE_ACL));

        final UserManagementPanel<AdminConsoleTableResources> userManagementPanel = new UserManagementPanel<>(userService, StringMessages.INSTANCE,
                SecuredDomainType.getAllInstances(), errorReporter, tableResources);
        userManagementPanel.ensureDebugId("UserManagementPanel");
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserManagementPanel<AdminConsoleTableResources>>(userManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userManagementPanel.updateUsers();
                        userManagementPanel.refreshSuggests();
                    }
                }, getStringMessages().userManagement(), SecuredSecurityTypes.USER.getPermission(DefaultActions.MUTATION_ACTIONS));

        final RoleDefinitionsPanel roleManagementPanel = new RoleDefinitionsPanel(StringMessages.INSTANCE,
                userService, tableResources, errorReporter);
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<RoleDefinitionsPanel>(roleManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        roleManagementPanel.updateRoleDefinitions();
                    }
                }, getStringMessages().roles(), SecuredSecurityTypes.ROLE_DEFINITION.getPermission(DefaultActions.MUTATION_ACTIONS));

        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanel(userService,
                StringMessages.INSTANCE, SecuredDomainType.getAllInstances(), errorReporter, tableResources);
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserGroupManagementPanel>(userGroupManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userGroupManagementPanel.updateUserGroups();
                        userGroupManagementPanel.refreshSuggests();
                    }
                }, getStringMessages().userGroupManagement(), SecuredSecurityTypes.USER_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        final FileStoragePanel fileStoragePanel = new FileStoragePanel(sailingService, errorReporter);
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<FileStoragePanel>(fileStoragePanel),
                getStringMessages().fileStorage(), SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_FILE_STORAGE, serverInfo));
        /* COURSE CREATION */
        final HorizontalTabLayoutPanel courseCreationTabPanel = adminConsolePanel
                .addVerticalTab(getStringMessages().courseCreation(), "CourseCreationTab");
        final MarkTemplatePanel markTemplatePanel = new MarkTemplatePanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkTemplatePanel>(markTemplatePanel) {
                @Override
                public void refreshAfterBecomingVisible() {
                            markTemplatePanel.refreshMarkTemplates();
                }
            }, getStringMessages().markTemplates(),
            SecuredDomainType.MARK_TEMPLATE.getPermission(DefaultActions.MUTATION_ACTIONS));
        final MarkPropertiesPanel markPropertiesPanel = new MarkPropertiesPanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkPropertiesPanel>(markPropertiesPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        markPropertiesPanel.refreshMarkProperties();
                    }
                }, getStringMessages().markProperties(),
                SecuredDomainType.MARK_PROPERTIES.getPermission(DefaultActions.MUTATION_ACTIONS));
        final CourseTemplatePanel courseTemplatePanel = new CourseTemplatePanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<CourseTemplatePanel>(courseTemplatePanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        courseTemplatePanel.refreshCourseTemplates();
                    }
                }, getStringMessages().courseTemplates(),
                SecuredDomainType.COURSE_TEMPLATE.getPermission(DefaultActions.MUTATION_ACTIONS));
        final MarkRolePanel markRolePanel = new MarkRolePanel(sailingService, errorReporter, getStringMessages(),
                userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkRolePanel>(markRolePanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        markRolePanel.refreshMarkRoles();
                    }
                }, getStringMessages().markRoles(),
                SecuredDomainType.MARK_ROLE.getPermission(DefaultActions.MUTATION_ACTIONS));
        adminConsolePanel.initUI(verticalTabName, horizontalTabName);
        presenter.fillRegattas();
        presenter.fillLeaderboardGroups();
        presenter.fillLeaderboards();
        return adminConsolePanel;
    }

}
