package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.security.SecuredDomainType.TrackedRaceActions;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.adminconsole.BoatPanel;
import com.sap.sailing.gwt.ui.adminconsole.BoatPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorPanel;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationPanel;
import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.EventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.EventManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.ExpeditionDeviceConfigurationsPanel;
import com.sap.sailing.gwt.ui.adminconsole.ExpeditionDeviceConfigurationsPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.IgtimiAccountsPanel;
import com.sap.sailing.gwt.ui.adminconsole.IgtimiAccountsPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupConfigPanel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupConfigPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.LocalServerManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.MediaPanel;
import com.sap.sailing.gwt.ui.adminconsole.MediaPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.RaceCourseManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.RaceCourseManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.RegattaManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.RegattaManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.RemoteServerInstancesManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.ResultImportUrlsListComposite;
import com.sap.sailing.gwt.ui.adminconsole.ResultImportUrlsListCompositeSupplier;
import com.sap.sailing.gwt.ui.adminconsole.SmartphoneTrackingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SmartphoneTrackingEventManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.StructureImportManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.StructureImportManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingEventManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingReplayConnectorPanel;
import com.sap.sailing.gwt.ui.adminconsole.SwissTimingReplayConnectorPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.TracTracEventManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.TracTracEventManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.TrackedRacesManagementPanel;
import com.sap.sailing.gwt.ui.adminconsole.TrackedRacesManagementPanelSupplier;
import com.sap.sailing.gwt.ui.adminconsole.WindPanel;
import com.sap.sailing.gwt.ui.adminconsole.WindPanelSupplier;
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
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.DefaultRefreshableAdminConsolePanel;
import com.sap.sse.gwt.adminconsole.ReplicationPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
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

    private ErrorReporter errorReporter;
    
    private AdminConsolePanel adminConsolePanel;
    
    private PlaceController placeController;
    
    private String verticalTabName;
    private String horizontalTabName;

    private AdminConsolePlace defaultPlace;
    
    public AdminConsoleViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
        
        this.userService = presenter.getUserService();
        this.sailingService = presenter.getSailingService();
        
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
                AdminConsolePanel panel = createAdminConsolePanel(serverInfo);
                if (defaultPlace != null) {
                    panel.selectTabByPlace(defaultPlace);
                }
                return panel;
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
        adminConsolePanel.selectTabByNamesWithoutSetup(verticalTabName, horizontalTabName);
    }
    
    @Override
    public void selectTabByPlace(AdminConsolePlace place) {
        adminConsolePanel.selectTabByPlace(place);
    }
    
    private AdminConsolePanel createAdminConsolePanel(final ServerInfoDTO serverInfo) {
        
        final Anchor pwaAnchor = new Anchor(
                new SafeHtmlBuilder().appendEscaped(getStringMessages().pwaAnchor()).toSafeHtml(), "AdminConsolePwa.html");
        pwaAnchor.addStyleName("releaseNotesAnchor");
        
        /**DOM.sinkEvents(awpAnchor, Event.ONCLICK);
        DOM.setEventListener(awpAnchor, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    event.preventDefault();
                    SwitchingEntryPoint.switchToMobile();
                }
            }
        });**/
        
        adminConsolePanel = new AdminConsolePanel(userService, 
                serverInfo, getStringMessages().releaseNotes(), "/release_notes_admin.html", pwaAnchor, errorReporter,
                SecurityStylesheetResources.INSTANCE.css(), stringMessages, placeController);
        adminConsolePanel.addStyleName("adminConsolePanel");
        
        /* EVENTS */
        final EventManagementPanelSupplier eventManagementPanelSupplier = new EventManagementPanelSupplier(stringMessages, presenter, adminConsolePanel);
        adminConsolePanel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                presenter.fillLeaderboardGroups();
            }
        }, getStringMessages().events(), new EventsPlace(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* REGATTAS */
        RegattaManagementPanelSupplier regattaManagementPanelSupplier = new RegattaManagementPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<RegattaManagementPanel>(regattaManagementPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillRegattas();
            }
        }, getStringMessages().regattas(), new RegattasPlace(), SecuredDomainType.REGATTA.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* LEADERBOARDS */
        final HorizontalTabLayoutPanel leaderboardTabPanel = adminConsolePanel
                .addVerticalTab(getStringMessages().leaderboards(), LEADERBOARDS);

        /* Leaderboard */
        LeaderboardConfigPanelSupplier leaderboardConfigPanelSupplier = new LeaderboardConfigPanelSupplier(
                stringMessages, presenter, true);
        adminConsolePanel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardConfigPanel>(leaderboardConfigPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillLeaderboards();
            }
        }, getStringMessages().leaderboards(), new LeaderboardsPlace(), SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.MUTATION_ACTIONS));     

        /* Leaderboard Group */
        LeaderboardGroupConfigPanelSupplier leaderboardGroupConfigPanelSupplier = new LeaderboardGroupConfigPanelSupplier(
                stringMessages, presenter);
        adminConsolePanel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardGroupConfigPanel>(leaderboardGroupConfigPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillLeaderboards();
                presenter.fillLeaderboardGroups();
            }

            @Override
            public void setupWidgetByParams(Map<String, String> params) {
                refreshAfterBecomingVisible(); //Refresh to sure that actual data is provided
                presenter.setupLeaderboardGroups(getWidget(), params);
            }
        }, getStringMessages().leaderboardGroups(), new LeaderboardGroupsPlace(), SecuredDomainType.LEADERBOARD_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        
        /* RACES */
        final HorizontalTabLayoutPanel racesTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().trackedRaces(), RACES);

        /* Tracked races */
        final TrackedRacesManagementPanelSupplier trackedRacesManagementPanelSupplier = new TrackedRacesManagementPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanelSupplier) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().trackedRaces(), new TrackedRacesPlace(),
                SecuredDomainType.TRACKED_RACE.getPermission(TrackedRaceActions.MUTATION_ACTIONS));

        /* Competitor */
        CompetitorPanelSupplier competitorPanelSupplier = new CompetitorPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors(), new CompetitorsPlace(), 
                SecuredDomainType.COMPETITOR.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        /* Boat */
        final BoatPanelSupplier boatPanelSupplier = new BoatPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<BoatPanel>(boatPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshBoatList();
            }
        }, getStringMessages().boats(), new BoatsPlace(),
                SecuredDomainType.BOAT.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        /* Race */
        RaceCourseManagementPanelSupplier raceCourseManagementPanelSupplier = 
                new RaceCourseManagementPanelSupplier(getStringMessages(), presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanelSupplier) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillRegattas();
                    }
                }, getStringMessages().courseLayout(), new CourseLayoutPlace(),
                SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));

        /* Wind */
        final WindPanelSupplier windPanelSupplier = new WindPanelSupplier(getStringMessages(), presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<WindPanel>(windPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                presenter.fillRegattas();
            }
        }, getStringMessages().wind(), new WindPlace(), SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));

        /* Media */
        final MediaPanelSupplier mediaPanelSupplier = new MediaPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel(), new AudioAndVideoPlace(), 
                SecuredDomainType.MEDIA_TRACK.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* RACE COMMITTEE APP */
        final HorizontalTabLayoutPanel raceCommitteeTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().raceCommitteeApp(), RACE_COMMITEE);

        /* Device Configuration User */
        final DeviceConfigurationPanelSupplier deviceConfigurationUserPanelSupplier = new DeviceConfigurationPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(raceCommitteeTabPanel,
                new DefaultRefreshableAdminConsolePanel<DeviceConfigurationPanel>(deviceConfigurationUserPanelSupplier),
                getStringMessages().deviceConfiguration(), new DeviceConfigurationPlace(),
                SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* CONNECTORS */
        final HorizontalTabLayoutPanel connectorsTabPanel = adminConsolePanel.addVerticalTab(getStringMessages().connectors(), CONNECTORS);

        /* TracTrac Event Management */
        TracTracEventManagementPanelSupplier tracTracEventManagementPanelSupplier = 
                new TracTracEventManagementPanelSupplier(stringMessages, presenter, tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<TracTracEventManagementPanel>(tracTracEventManagementPanelSupplier) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        getWidget().refreshTracTracConnectors();
                    }
                },
                getStringMessages().tracTracEvents(), new TracTracEventsPlace(),
                SecuredDomainType.TRACTRAC_ACCOUNT.getPermission(DefaultActions.values()));

        /* Swiss Timing Replay Connector */
        SwissTimingReplayConnectorPanelSupplier swissTimingReplayConnectorPanelSupplier = 
                new SwissTimingReplayConnectorPanelSupplier(stringMessages, presenter, tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(
                        swissTimingReplayConnectorPanelSupplier),
                getStringMessages().swissTimingArchiveConnector(), new SwissTimingArchivedEventsPlace(),
                SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT.getPermission(DefaultActions.values()));

        /* Swiss Timing Event Management */
        SwissTimingEventManagementPanelSupplier swissTimingEventManagementPanelSupplier = 
                new SwissTimingEventManagementPanelSupplier(stringMessages, presenter, tableResources);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swissTimingEventManagementPanelSupplier),
                getStringMessages().swissTimingEvents(), new SwissTimingEventsPlace(),
                SecuredDomainType.SWISS_TIMING_ACCOUNT.getPermission(DefaultActions.values()));

        /* Smartphone Tracking Event Management */
        SmartphoneTrackingEventManagementPanelSupplier trackingEventManagementPanelSupplier = 
                new SmartphoneTrackingEventManagementPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<SmartphoneTrackingEventManagementPanel>(
                        trackingEventManagementPanelSupplier) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        presenter.fillLeaderboards();
                    }
                }, getStringMessages().smartphoneTracking(), new SmartphoneTrackingPlace(),
                SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.UPDATE, DefaultActions.DELETE));

        /* Igtimi Accounts */
        IgtimiAccountsPanelSupplier accountsPanelSupplier = new IgtimiAccountsPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<IgtimiAccountsPanel>(accountsPanelSupplier) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        getWidget().refresh();
                    }
                }, getStringMessages().igtimiAccounts(), new IgtimiAccountsPlace(),
                SecuredDomainType.IGTIMI_ACCOUNT.getPermission(DefaultActions.values()));

        /* Expedition Device Configurations */
        ExpeditionDeviceConfigurationsPanelSupplier expeditionDeviceConfigurationsPanelSupplier = 
                new ExpeditionDeviceConfigurationsPanelSupplier(stringMessages, presenter);
        adminConsolePanel.addToTabPanel(connectorsTabPanel, 
                new DefaultRefreshableAdminConsolePanel<ExpeditionDeviceConfigurationsPanel>(expeditionDeviceConfigurationsPanelSupplier) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refresh();
            }
        }, getStringMessages().expeditionDeviceConfigurations(), new ExpeditionDeviceConfigurationsPlace(),
                SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getPermission(DefaultActions.values()));

        /* Result Import Urls List */
        ResultImportUrlsListCompositeSupplier urlsListCompositeSupplier = new ResultImportUrlsListCompositeSupplier(
                stringMessages, presenter);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<ResultImportUrlsListComposite>(urlsListCompositeSupplier),
                getStringMessages().resultImportUrls(), new ResultImportUrlsPlace(),
                SecuredDomainType.RESULT_IMPORT_URL.getPermission(DefaultActions.values()));

        /* Structure Import Management */
        StructureImportManagementPanelSupplier structureImportManagementPanelSupplier = new StructureImportManagementPanelSupplier(
                stringMessages, presenter);
        adminConsolePanel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(
                        structureImportManagementPanelSupplier),
                getStringMessages().manage2Sail() + " " + getStringMessages().regattaStructureImport(),
                new Manage2SailRegattaStructureImportPlace(),
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
        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(presenter, stringMessages);
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
                }, getStringMessages().userManagement(), new UserManagementPlace(), SecuredSecurityTypes.USER.getPermission(DefaultActions.MUTATION_ACTIONS));

        final RoleDefinitionsPanel roleManagementPanel = new RoleDefinitionsPanel(StringMessages.INSTANCE,
                userService, tableResources, errorReporter);
        adminConsolePanel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<RoleDefinitionsPanel>(roleManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        roleManagementPanel.updateRoleDefinitions();
                    }
                }, getStringMessages().roles(), new RolesPlace(), 
                SecuredSecurityTypes.ROLE_DEFINITION.getPermission(DefaultActions.MUTATION_ACTIONS));

        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanel(userService,
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
        adminConsolePanel.initUI(verticalTabName, horizontalTabName);

        return adminConsolePanel;
    }

    @Override
    public void setRedirectToPlace(AdminConsolePlace redirectoPlace) {
        this.defaultPlace = redirectoPlace;
    }

}
