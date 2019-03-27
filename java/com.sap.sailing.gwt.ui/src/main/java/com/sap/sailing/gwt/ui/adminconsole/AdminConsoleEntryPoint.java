package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.security.SecuredDomainType.TrackedRaceActions;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
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
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.DefaultRefreshableAdminConsolePanel;
import com.sap.sse.gwt.adminconsole.ReplicationPanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.filestorage.FileStoragePanel;
import com.sap.sse.gwt.client.panels.HorizontalTabLayoutPanel;
import com.sap.sse.gwt.resources.Highcharts;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.client.component.RoleDefinitionsPanel;
import com.sap.sse.security.ui.client.component.UserGroupManagementPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;

public class AdminConsoleEntryPoint extends AbstractSailingEntryPoint
        implements RegattaRefresher, LeaderboardsRefresher<StrippedLeaderboardDTOWithSecurity>, LeaderboardGroupsRefresher {
    private final AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
        runWithServerInfo(serverInfo->createUI(serverInfo));
    }
     
    private void createUI(final ServerInfoDTO serverInfo) {
        HeaderPanel headerPanel = new HeaderPanel();
        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().administration());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
        authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
            @Override
            public Widget get() {
                checkPublicServerNonPublicUserWarning();
                return createAdminConsolePanel(serverInfo);
            }
        });
        headerPanel.setHeaderWidget(header);
        headerPanel.setContentWidget(authorizedContentDecorator);
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        rootPanel.add(headerPanel);
    }
    
    protected void checkPublicServerNonPublicUserWarning() {
        getSailingService().getServerConfiguration(new AsyncCallback<ServerConfigurationDTO>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ServerConfigurationDTO result) {
                if (Boolean.TRUE.equals(result.isPublic())) {
                    StrippedUserGroupDTO currentTenant = getUserService().getCurrentTenant();
                    StrippedUserGroupDTO serverTenant = result.getServerDefaultTenant();
                    if (!serverTenant.equals(currentTenant)) {
                        if (getUserService().getCurrentUser().getUserGroups().contains(serverTenant)) {
                            // The current user is in server tenant group and so his default tenant could be changed.
                            if (Window.confirm(getStringMessages().serverIsPublicButTenantIsNotAndCouldBeChanged())) {
                                // change the default tenant
                                changeDefaultTenantForCurrentUser(serverTenant);
                            }
                        } else {
                            // The current user is not in the server tenant group so his default tenant cannot be
                            // changed.
                            Window.alert(getStringMessages().serverIsPublicButTenantIsNot());
                        }
                    }
                }
            }

            /** Changes the default tenant for the current user. */
            private void changeDefaultTenantForCurrentUser(final StrippedUserGroupDTO serverTenant) {
                final UserDTO user = getUserService().getCurrentUser();
                getUserService().getUserManagementService().updateUserProperties(user.getName(), user.getFullName(),
                        user.getCompany(), user.getLocale(), serverTenant.getId().toString(),
                        new AsyncCallback<UserDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(UserDTO result) {
                                user.setDefaultTenantForCurrentServer(serverTenant);
                            }
                        });
            }
        });
    }

    private Widget createAdminConsolePanel(ServerInfoDTO serverInfo) {
        AdminConsolePanel panel = new AdminConsolePanel(getUserService(), 
                serverInfo, getStringMessages().releaseNotes(), "/release_notes_admin.html", /* error reporter */ this,
                SecurityStylesheetResources.INSTANCE.css(), getStringMessages());
        panel.addStyleName("adminConsolePanel");
        
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();
        
        /* EVENTS */
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(getSailingService(),
                getUserService(), this, this, getStringMessages(), panel);
        eventManagementPanel.ensureDebugId("EventManagement");
        panel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                fillLeaderboardGroups();
            }
        }, getStringMessages().events(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));
        leaderboardGroupsDisplayers.add(eventManagementPanel);
        
        /* REGATTAS */
        RegattaManagementPanel regattaManagementPanel = new RegattaManagementPanel(
                getSailingService(), getUserService(), this, getStringMessages(), this, eventManagementPanel);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        panel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<RegattaManagementPanel>(regattaManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillRegattas();
            }
        }, getStringMessages().regattas(), SecuredDomainType.REGATTA.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        final HorizontalTabLayoutPanel leaderboardTabPanel = panel.addVerticalTab(getStringMessages().leaderboards(), "LeaderboardPanel");
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(getSailingService(), getUserService(), this, this,
                getStringMessages(), /* showRaceDetails */true, this);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        panel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardConfigPanel>(leaderboardConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
            }
        }, getStringMessages().leaderboards(), SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(
                getSailingService(), getUserService(), this, this, this, this, getStringMessages());
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        panel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardGroupConfigPanel>(leaderboardGroupConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
                fillLeaderboardGroups();
            }

            @Override
            public void setupWidgetByParams(Map<String, String> params) {
                refreshAfterBecomingVisible(); //Refresh to sure that actual data is provided
                setupLeaderboardGroups(leaderboardGroupConfigPanel, params);
            }
        }, getStringMessages().leaderboardGroups(), SecuredDomainType.LEADERBOARD_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);
        
        /* RACES */
        final HorizontalTabLayoutPanel racesTabPanel = panel.addVerticalTab(getStringMessages().trackedRaces(), "RacesPanel");

        final TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(
                getSailingService(), getUserService(), this, this, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        panel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        fillRegattas();
                    }
                }, getStringMessages().trackedRaces(),
                SecuredDomainType.TRACKED_RACE.getPermission(TrackedRaceActions.MUTATION_ACTIONS));
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(getSailingService(), getUserService(), getStringMessages(), this);
        competitorPanel.ensureDebugId("CompetitorPanel");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors(),
                SecuredDomainType.COMPETITOR.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        final BoatPanel boatPanel = new BoatPanel(getSailingService(), getUserService(), getStringMessages(), this);
        boatPanel.ensureDebugId("BoatPanel");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<BoatPanel>(boatPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshBoatList();
            }
        }, getStringMessages().boats(),
                SecuredDomainType.BOAT.getPermission(DefaultActions.MUTATION_ACTIONS_FOR_NON_DELETABLE_TYPES));

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(getSailingService(), this,
                this, getStringMessages(), getUserService());
        panel.addToTabPanel(racesTabPanel,
                new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        fillRegattas();
                    }
                }, getStringMessages().courseLayout(),
                SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(getSailingService(), getUserService(), asyncActionsExecutor, this,
                getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<WindPanel>(windPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillRegattas();
            }
        }, getStringMessages().wind(), SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.UPDATE));

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, getSailingService(), this, mediaService, this,
                getStringMessages(), getUserService());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel(),
                SecuredDomainType.MEDIA_TRACK.getPermission(DefaultActions.MUTATION_ACTIONS));

        /* RACE COMMITTEE APP */
        final HorizontalTabLayoutPanel raceCommitteeTabPanel = panel.addVerticalTab(getStringMessages().raceCommitteeApp(), "RaceCommiteeAppPanel");
        final DeviceConfigurationPanel deviceConfigurationUserPanel = new DeviceConfigurationPanel(
                getSailingService(), getUserService(), getStringMessages(), this);
        panel.addToTabPanel(raceCommitteeTabPanel,
                new DefaultRefreshableAdminConsolePanel<DeviceConfigurationPanel>(deviceConfigurationUserPanel),
                getStringMessages().deviceConfiguration(),
                SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION.getPermission(DefaultActions.MUTATION_ACTIONS));
        
        /* CONNECTORS */
        final HorizontalTabLayoutPanel connectorsTabPanel = panel.addVerticalTab(getStringMessages().connectors(), "TrackingProviderPanel");
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(
                getSailingService(), getUserService(), this, this, getStringMessages());
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<TracTracEventManagementPanel>(tractracEventManagementPanel),
                getStringMessages().tracTracEvents(),
                SecuredDomainType.TRACTRAC_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(tractracEventManagementPanel);
        
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                getSailingService(), getUserService(), this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(swissTimingReplayConnectorPanel),
                getStringMessages().swissTimingArchiveConnector(),
                SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                getSailingService(), getUserService(), this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swisstimingEventManagementPanel),
                getStringMessages().swissTimingEvents(),
                SecuredDomainType.SWISS_TIMING_ACCOUNT.getPermission(DefaultActions.values()));
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final SmartphoneTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new SmartphoneTrackingEventManagementPanel(
                getSailingService(), getUserService(), this, this, this, getStringMessages());
        raceLogTrackingEventManagementPanel.ensureDebugId("SmartphoneTrackingPanel");
        panel.addToTabPanel(connectorsTabPanel,
                new DefaultRefreshableAdminConsolePanel<SmartphoneTrackingEventManagementPanel>(
                        raceLogTrackingEventManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        fillLeaderboards();
                    }
                }, getStringMessages().smartphoneTracking(),
                SecuredDomainType.LEADERBOARD.getPermission(DefaultActions.UPDATE, DefaultActions.DELETE));
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(getSailingService(), this, getStringMessages());
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<IgtimiAccountsPanel>(igtimiAccountsPanel),
                getStringMessages().igtimiAccounts(),
                SecuredDomainType.IGTIMI_ACCOUNT.getPermission(DefaultActions.values()));
        ExpeditionDeviceConfigurationsPanel expeditionDeviceConfigurationsPanel = new ExpeditionDeviceConfigurationsPanel(getSailingService(), this, getStringMessages());
        expeditionDeviceConfigurationsPanel.ensureDebugId("ExpeditionDeviceConfigurations");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ExpeditionDeviceConfigurationsPanel>(expeditionDeviceConfigurationsPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                expeditionDeviceConfigurationsPanel.refresh();
            }
        }, getStringMessages().expeditionDeviceConfigurations(),
                SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getPermission(DefaultActions.values()));

        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(
                getSailingService(), getUserService(), this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ResultImportUrlsManagementPanel>(resultImportUrlsManagementPanel),
                getStringMessages().resultImportUrls(),
                SecuredDomainType.RESULT_IMPORT_URL.getPermission(DefaultActions.values()));
        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(
                getSailingService(), getUserService(), this, getStringMessages(), this, eventManagementPanel);
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(structureImportUrlsManagementPanel),
                getStringMessages().manage2Sail() + " " + getStringMessages().regattaStructureImport(),
                SecuredDomainType.REGATTA.getPermission(DefaultActions.CREATE)); // TODO bug4763 provide the default CREATE ownership for REGATTA / EVENT

        /* ADVANCED */
        final HorizontalTabLayoutPanel advancedTabPanel = panel.addVerticalTab(getStringMessages().advanced(),
                "AdvancedTab");
        final ReplicationPanel replicationPanel = new ReplicationPanel(getSailingService(), this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<ReplicationPanel>(replicationPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                replicationPanel.updateReplicaList();
            }
        }, getStringMessages().replication(), SecuredDomainType.REPLICATOR.getPermission()); // TODO bug4754 use server name as type-relative object identifier

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(getStringMessages(), getSailingService(),
                this, eventManagementPanel, this, this, mediaPanel);
        masterDataImportPanel.ensureDebugId("MasterDataImport");
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<MasterDataImportPanel>(masterDataImportPanel),
                getStringMessages().masterDataImportPanel(), SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.IMPORT_MASTER_DATA, serverInfo));

        RemoteServerInstancesManagementPanel remoteServerInstancesManagementPanel = new RemoteServerInstancesManagementPanel(getSailingService(), this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<RemoteServerInstancesManagementPanel>(remoteServerInstancesManagementPanel),
                getStringMessages().remoteServerInstances(),
                SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_REMOTE_INSTANCES, serverInfo));

        LocalServerManagementPanel localServerInstancesManagementPanel = new LocalServerManagementPanel(
                getSailingService(), getUserService(), this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<LocalServerManagementPanel>(
                        localServerInstancesManagementPanel),
                getStringMessages().localServer(),
                // We explicitly use a different permission check here.
                // Most panels show a list of domain objects which means we check if the user has permissions for any
                // potentially existing object to decide about the visibility.
                // The local server tab is about the specific server object. This check needs to contain the ownership
                // information to work as intended.
                () -> getUserService().hasAnyServerPermission(ServerActions.CONFIGURE_LOCAL_SERVER,
                        DefaultActions.CHANGE_OWNERSHIP, DefaultActions.CHANGE_ACL));

        final UserManagementPanel<AdminConsoleTableResources> userManagementPanel = new UserManagementPanel<>(getUserService(), StringMessages.INSTANCE,
                SecuredDomainType.getAllInstances(), this, tableResources);
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserManagementPanel<AdminConsoleTableResources>>(userManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userManagementPanel.updateUsers();
                    }
                }, getStringMessages().userManagement(), SecuredSecurityTypes.USER.getPermission(DefaultActions.MUTATION_ACTIONS));

        final RoleDefinitionsPanel roleManagementPanel = new RoleDefinitionsPanel(StringMessages.INSTANCE,
                getUserService(), tableResources, this);
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<RoleDefinitionsPanel>(roleManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        roleManagementPanel.updateRoleDefinitions();
                    }
                }, getStringMessages().roles(), SecuredSecurityTypes.ROLE_DEFINITION.getPermission(DefaultActions.MUTATION_ACTIONS));

        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanel(getUserService(),
                StringMessages.INSTANCE, SecuredDomainType.getAllInstances(), this, tableResources);
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserGroupManagementPanel>(userGroupManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userGroupManagementPanel.updateUserGroups();
                    }
                }, getStringMessages().userGroupManagement(), SecuredSecurityTypes.USER_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));

        final FileStoragePanel fileStoragePanel = new FileStoragePanel(getSailingService(), this);
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<FileStoragePanel>(fileStoragePanel),
                getStringMessages().fileStorage(), SecuredSecurityTypes.SERVER.getPermissionForObject(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_FILE_STORAGE, serverInfo));
        panel.initUI();
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();
        return panel;
    }

    @Override
    public void fillLeaderboards() {
        getSailingService().getLeaderboardsWithSecurity(new MarkedAsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>(
                new AsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>() {
                    @Override
                    public void onSuccess(List<StrippedLeaderboardDTOWithSecurity> leaderboards) {
                        for (LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer : leaderboardsDisplayers) {
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
    public void updateLeaderboards(Iterable<StrippedLeaderboardDTOWithSecurity> updatedLeaderboards,
            LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> origin) {
        for (LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer : leaderboardsDisplayers) {
            if (leaderboardsDisplayer != origin) {
                leaderboardsDisplayer.fillLeaderboards(updatedLeaderboards);
            }
        }
    }

    @Override
    public void fillLeaderboardGroups() {
        getSailingService().getLeaderboardGroups(false /*withGeoLocationData*/,
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
        getSailingService().getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
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
    
    @Override
    public void setupLeaderboardGroups(LeaderboardGroupsDisplayer displayer, Map<String, String> params) {
        displayer.setupLeaderboardGroups(params);
    }
}
