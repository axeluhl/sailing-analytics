package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
import com.sap.sailing.gwt.ui.adminconsole.RoleDefinitionsPanelWrapper;
import com.sap.sailing.gwt.ui.adminconsole.SmartphoneTrackingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.StructureImportManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingReplayConnectorPanel;
import com.sap.sailing.gwt.ui.adminconsole.TracTracEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.TrackedRacesManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.UserGroupManagementPanelWrapper;
import com.sap.sailing.gwt.ui.adminconsole.UserManagementPanelWrapper;
import com.sap.sailing.gwt.ui.adminconsole.WindPanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.CourseTemplatePanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkPropertiesPanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkRolePanel;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.MarkTemplatePanel;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.FileStoragePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.LocalServerPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.MasterDataImportPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.RemoteServerInstancesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.ReplicationPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.RolesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.UserGroupManagementPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.advanced.UserManagementPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.ExpeditionDeviceConfigurationsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.IgtimiAccountsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.Manage2SailRegattaStructureImportPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.ResultImportUrlsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.SmartphoneTrackingPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.SwissTimingArchivedEventsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.SwissTimingEventsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.connectors.TracTracEventsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.coursecreation.CourseTemplatesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.coursecreation.MarkPropertiesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.coursecreation.MarkRolesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.coursecreation.MarkTemplatesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.events.EventsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.leaderboards.LeaderboardGroupsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.leaderboards.LeaderboardsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.racemanager.DeviceConfigurationPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.regattas.RegattasPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.AudioAndVideoPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.BoatsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.CompetitorsPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.CourseLayoutPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.TrackedRacesPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.trackedraces.WindPlace;
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
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;
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

    interface AdminConsoleViewUiBinder extends UiBinder<Widget, AdminConsoleViewImpl> {
    }
    private static AdminConsoleViewUiBinder uiBinder = GWT.create(AdminConsoleViewUiBinder.class);    
    private final AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
    
    public static final String ADVANCED = "AdvancedTab";
    public static final String CONNECTORS = "TrackingProviderPanel";
    public static final String COURSE_CREATION = "CourseCreationTab";
    public static final String LEADERBOARDS = "LeaderboardPanel";
    public static final String RACES = "RacesPanel";
    public static final String RACE_COMMITEE = "RaceCommiteeAppPanel"; 
    
    @UiField
    HeaderPanel headerPanel;
    
    private Presenter presenter;    
    private StringMessages stringMessages;   
    private UserService userService;    
    private SailingServiceWriteAsync sailingService;
    private MediaServiceWriteAsync mediaServiceWrite;   
    private ErrorReporter errorReporter;
   
    private AdminConsolePanel adminConsolePanel;    
    private PlaceController placeController;
    private AdminConsolePlace defaultPlace;
    
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers; 
    
    public AdminConsoleViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
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
        this.placeController = presenter.getPlaceController();
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
    public void selectTabByPlace(AdminConsolePlace place) {
        adminConsolePanel.selectTabByPlace(place);
    }
    
    private AdminConsolePanel createAdminConsolePanel(final ServerInfoDTO serverInfo) {
        
        adminConsolePanel = new AdminConsolePanel(userService, 
                serverInfo, getStringMessages().releaseNotes(), "/release_notes_admin.html", null, errorReporter,
                SecurityStylesheetResources.INSTANCE.css(), stringMessages, placeController);
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
        }, getStringMessages().events(), new EventsPlace(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));
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
        }, getStringMessages().regattas(), new RegattasPlace(), SecuredDomainType.REGATTA.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        final HorizontalTabLayoutPanel leaderboardTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().leaderboards(), LEADERBOARDS);
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, userService, presenter, errorReporter,
                getStringMessages(), /* showRaceDetails */true, presenter);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        adminConsolePanel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardConfigPanel>(leaderboardConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillLeaderboards();
            }
        }, getStringMessages().leaderboards(), new LeaderboardsPlace(), SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.MUTATION_ACTIONS));     
        
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
        }, getStringMessages().leaderboardGroups(), new LeaderboardGroupsPlace(), SecuredDomainType.LEADERBOARD_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);
        
        /* RACES */
        final HorizontalTabLayoutPanel racesTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().trackedRaces(), RACES);

        final TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().trackedRaces(), new TrackedRacesPlace(),
                SecuredDomainType.TRACKED_RACE.getPermission(TrackedRaceActions.MUTATION_ACTIONS));
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, userService, getStringMessages(), errorReporter);
        competitorPanel.ensureDebugId("CompetitorPanel");
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors(), new CompetitorsPlace(), 
                SecuredDomainType.COMPETITOR.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        final BoatPanel boatPanel = new BoatPanel(sailingService, userService, getStringMessages(), errorReporter);
        boatPanel.ensureDebugId("BoatPanel");
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<BoatPanel>(boatPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshBoatList();
            }
        }, getStringMessages().boats(), new BoatsPlace(),
                SecuredDomainType.BOAT.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, errorReporter,
                presenter, getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().courseLayout(), new CourseLayoutPlace(),
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
        }, getStringMessages().wind(), new WindPlace(), SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));
        regattasDisplayers.add(windPanel);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, presenter, mediaServiceWrite, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel(), new AudioAndVideoPlace(), 
                SecuredDomainType.MEDIA_TRACK.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* RACE COMMITTEE APP */
        final HorizontalTabLayoutPanel raceCommitteeTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().raceCommitteeApp(), RACE_COMMITEE);
        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationPanel(
                sailingService, userService, getStringMessages(), errorReporter);
        adminConsolePanel.addToTabPanel(raceCommitteeTabPanel,
                new DefaultRefreshableAdminConsolePanel<DeviceConfigurationPanel>(deviceConfigurationUserPanel),
                getStringMessages().deviceConfiguration(), new DeviceConfigurationPlace(),
                SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION.getPermission(DefaultActions.MUTATION_ACTIONS));
        
        /* CONNECTORS */
        final HorizontalTabLayoutPanel connectorsTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().connectors(), CONNECTORS);
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
                getStringMessages().tracTracEvents(), new TracTracEventsPlace(),
                SecuredDomainType.TRACTRAC_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(tractracEventManagementPanel);
        
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(swissTimingReplayConnectorPanel),
                getStringMessages().swissTimingArchiveConnector(), new SwissTimingArchivedEventsPlace(),
                SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, userService, errorReporter, presenter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swisstimingEventManagementPanel),
                getStringMessages().swissTimingEvents(), new SwissTimingEventsPlace(),
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
                }, getStringMessages().smartphoneTracking(), new SmartphoneTrackingPlace(),
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
                getStringMessages().igtimiAccounts(), new IgtimiAccountsPlace(),
                SecuredDomainType.IGTIMI_ACCOUNT.getPermission(DefaultActions.values()));
        ExpeditionDeviceConfigurationsPanel expeditionDeviceConfigurationsPanel = new ExpeditionDeviceConfigurationsPanel(sailingService, errorReporter, getStringMessages(), userService);
        expeditionDeviceConfigurationsPanel.ensureDebugId("ExpeditionDeviceConfigurations");
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ExpeditionDeviceConfigurationsPanel>(expeditionDeviceConfigurationsPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                expeditionDeviceConfigurationsPanel.refresh();
            }
        }, getStringMessages().expeditionDeviceConfigurations(), new ExpeditionDeviceConfigurationsPlace(),
                SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getPermission(DefaultActions.values()));

        ResultImportUrlsListComposite resultUrlsListComposite = new ResultImportUrlsListComposite(sailingService,
                userService, errorReporter, getStringMessages());
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<ResultImportUrlsListComposite>(resultUrlsListComposite),
                getStringMessages().resultImportUrls(), new ResultImportUrlsPlace(),
                SecuredDomainType.RESULT_IMPORT_URL.getPermission(DefaultActions.values()));

        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(
                sailingService, userService, errorReporter, getStringMessages(), presenter, eventManagementPanel);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(structureImportUrlsManagementPanel),
                getStringMessages().manage2Sail() + " " + getStringMessages().regattaStructureImport(), new Manage2SailRegattaStructureImportPlace(),
                SecuredDomainType.REGATTA.getPermission(DefaultActions.CREATE));

        /* ADVANCED */
        final HorizontalTabLayoutPanel advancedTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().advanced(),
                ADVANCED);
        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, userService, errorReporter,
                getStringMessages());
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<ReplicationPanel>(replicationPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                replicationPanel.updateReplicaList();
            }
                }, getStringMessages().replication(), new ReplicationPlace(),
                () -> userService.hasAnyServerPermission(ServerActions.REPLICATE, ServerActions.START_REPLICATION,
                        ServerActions.READ_REPLICATOR));
        
        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(getStringMessages(), sailingService,
                presenter, eventManagementPanel, presenter, presenter, mediaPanel);
        masterDataImportPanel.ensureDebugId("MasterDataImport");
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<MasterDataImportPanel>(masterDataImportPanel),
                getStringMessages().masterDataImportPanel(), new MasterDataImportPlace(), SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CAN_IMPORT_MASTERDATA, serverInfo));
        
        RemoteServerInstancesManagementPanel remoteServerInstancesManagementPanel = new RemoteServerInstancesManagementPanel(sailingService, userService,
                errorReporter, getStringMessages(), tableResources);
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<RemoteServerInstancesManagementPanel>(remoteServerInstancesManagementPanel),
                getStringMessages().remoteServerInstances(), new RemoteServerInstancesPlace(),
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
        }, getStringMessages().localServer(), new LocalServerPlace(), 
                // We explicitly use a different permission check here.
                // Most panels show a list of domain objects which means we check if the user has permissions for any
                // potentially existing object to decide about the visibility.
                // The local server tab is about the specific server object. This check needs to contain the ownership
                // information to work as intended.
                () -> userService.hasAnyServerPermission(ServerActions.CONFIGURE_LOCAL_SERVER,
                        DefaultActions.CHANGE_OWNERSHIP, DefaultActions.CHANGE_ACL));

        final UserManagementPanel<AdminConsoleTableResources> userManagementPanel = new UserManagementPanelWrapper(userService, StringMessages.INSTANCE,
                SecuredDomainType.getAllInstances(), errorReporter, tableResources);
        userManagementPanel.ensureDebugId("UserManagementPanel");
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserManagementPanel<AdminConsoleTableResources>>(userManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userManagementPanel.updateUsers();
                        userManagementPanel.refreshSuggests();
                    }
                }, getStringMessages().userManagement(), new UserManagementPlace(), SecuredSecurityTypes.USER.getPermission(DefaultActions.MUTATION_ACTIONS));

        final RoleDefinitionsPanel roleManagementPanel = new RoleDefinitionsPanelWrapper(StringMessages.INSTANCE,
                userService, tableResources, errorReporter);
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<RoleDefinitionsPanel>(roleManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        roleManagementPanel.updateRoleDefinitions();
                    }
                }, getStringMessages().roles(), new RolesPlace(), 
                SecuredSecurityTypes.ROLE_DEFINITION.getPermission(DefaultActions.MUTATION_ACTIONS));

        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanelWrapper(userService,
                StringMessages.INSTANCE, SecuredDomainType.getAllInstances(), errorReporter, tableResources);
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserGroupManagementPanel>(userGroupManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userGroupManagementPanel.updateUserGroups();
                        userGroupManagementPanel.refreshSuggests();
                    }
                }, getStringMessages().userGroupManagement(), new UserGroupManagementPlace(), SecuredSecurityTypes.USER_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        final FileStoragePanel fileStoragePanel = new FileStoragePanel(sailingService, errorReporter);
        adminConsolePanel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<FileStoragePanel>(fileStoragePanel),
                getStringMessages().fileStorage(), new FileStoragePlace(),
                SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_FILE_STORAGE, serverInfo));
        
        /* COURSE CREATION */
        final HorizontalTabLayoutPanel courseCreationTabPanel = adminConsolePanel
                .addVerticalTab(getStringMessages().courseCreation(), COURSE_CREATION);
        final MarkTemplatePanel markTemplatePanel = new MarkTemplatePanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkTemplatePanel>(markTemplatePanel) {
                @Override
                public void refreshAfterBecomingVisible() {
                            markTemplatePanel.refreshMarkTemplates();
                }
            }, getStringMessages().markTemplates(), new MarkTemplatesPlace(),
            SecuredDomainType.MARK_TEMPLATE.getPermission(DefaultActions.MUTATION_ACTIONS));
        final MarkPropertiesPanel markPropertiesPanel = new MarkPropertiesPanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkPropertiesPanel>(markPropertiesPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        markPropertiesPanel.refreshMarkProperties();
                    }
                }, getStringMessages().markProperties(), new MarkPropertiesPlace(),
                SecuredDomainType.MARK_PROPERTIES.getPermission(DefaultActions.MUTATION_ACTIONS));
        final CourseTemplatePanel courseTemplatePanel = new CourseTemplatePanel(sailingService, errorReporter,
                getStringMessages(), userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<CourseTemplatePanel>(courseTemplatePanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        courseTemplatePanel.refreshCourseTemplates();
                    }
                }, getStringMessages().courseTemplates(), new CourseTemplatesPlace(),
                SecuredDomainType.COURSE_TEMPLATE.getPermission(DefaultActions.MUTATION_ACTIONS));
        final MarkRolePanel markRolePanel = new MarkRolePanel(sailingService, errorReporter, getStringMessages(),
                userService);
        adminConsolePanel.addToTabPanel(courseCreationTabPanel,
                new DefaultRefreshableAdminConsolePanel<MarkRolePanel>(markRolePanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        markRolePanel.refreshMarkRoles();
                    }
                }, getStringMessages().markRoles(), new MarkRolesPlace(), 
                SecuredDomainType.MARK_ROLE.getPermission(DefaultActions.MUTATION_ACTIONS));

        adminConsolePanel.initUI(defaultPlace);
        
        presenter.fillRegattas();
        presenter.fillLeaderboardGroups();
        presenter.fillLeaderboards();
        
        return adminConsolePanel;
    }

    @Override
    public void setRedirectToPlace(AdminConsolePlace redirectoPlace) {
        this.defaultPlace = redirectoPlace;
    }

}
