package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
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
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
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
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.client.component.RoleDefinitionsPanel;
import com.sap.sse.security.ui.client.component.UserGroupManagementPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;

public class AdminConsoleEntryPoint extends AbstractSailingEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private final AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
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
                return createAdminConsolePanel(serverInfo);
            }
        });
        headerPanel.setHeaderWidget(header);
        headerPanel.setContentWidget(authorizedContentDecorator);
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        rootPanel.add(headerPanel);
    }
    
    private Widget createAdminConsolePanel(ServerInfoDTO serverInfo) {
        AdminConsolePanel panel = new AdminConsolePanel(getUserService(), 
                serverInfo, getStringMessages().releaseNotes(), "/release_notes_admin.html", /* error reporter */ this,
                SecurityStylesheetResources.INSTANCE.css(), getStringMessages());
        panel.addStyleName("adminConsolePanel");
        
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();
        
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(getSailingService(), getUserService(), this, this, getStringMessages(), panel);
        eventManagementPanel.ensureDebugId("EventManagement");
        panel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                fillLeaderboardGroups();
            }
        }, getStringMessages().events()); // no permissions required; we show those events the user may read
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
        }, getStringMessages().regattas(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));
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
        racesTabPanel.ensureDebugId("RacesPanel");

        final TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(
                getSailingService(), getUserService(), this, this, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillRegattas();
            }
        }, getStringMessages().trackedRaces(), SecuredDomainType.TRACKED_RACE.getPermission(DefaultActions.MUTATION_ACTIONS));
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(getSailingService(), getUserService(), getStringMessages(), this);
        competitorPanel.ensureDebugId("CompetitorPanel");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors()); // no permissions required; we show those competitors the user may read

        final BoatPanel boatPanel = new BoatPanel(getSailingService(), getUserService(), getStringMessages(), this);
        boatPanel.ensureDebugId("BoatPanel");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<BoatPanel>(boatPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshBoatList();
            }
        }, getStringMessages().boats()); // no permissions required; we show those boats the user may read

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(getSailingService(), this, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanel), getStringMessages().courseLayout());
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(getSailingService(), getUserService(), asyncActionsExecutor, this, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<WindPanel>(windPanel), getStringMessages().wind()); // no permissions required; we show those wind the user may read
        regattasDisplayers.add(windPanel);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, getSailingService(), this, mediaService, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel()); // no permissions required; we show those media the user may read

        /* RACE COMMITTEE APP */

        final HorizontalTabLayoutPanel raceCommitteeTabPanel = panel.addVerticalTab(getStringMessages().raceCommitteeApp(), "RaceCommiteeAppPanel");
        final DeviceConfigurationUserPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(getSailingService(),
                getUserService(), getStringMessages(), this);
        panel.addToTabPanel(raceCommitteeTabPanel, new DefaultRefreshableAdminConsolePanel<DeviceConfigurationUserPanel>(deviceConfigurationUserPanel),
                getStringMessages().deviceConfiguration()); // no permissions required; we show those device configurations the user may read
        
        /* CONNECTORS */
        
        final HorizontalTabLayoutPanel connectorsTabPanel = panel.addVerticalTab(getStringMessages().connectors(), "TrackingProviderPanel");
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(getSailingService(),
                this, this, getStringMessages());
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<TracTracEventManagementPanel>(tractracEventManagementPanel),
                getStringMessages().tracTracEvents()); // no permissions required; we show those TracTrac connector stuff the user may read
        regattasDisplayers.add(tractracEventManagementPanel);
        
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                getSailingService(), this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(swissTimingReplayConnectorPanel),
                getStringMessages().swissTimingArchiveConnector()); // no permissions required; we show those SwissTiming connector stuff the user may read
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                getSailingService(), this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swisstimingEventManagementPanel),
                getStringMessages().swissTimingEvents()); // no permissions required; we show those SwissTiming connector stuff the user may read
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final SmartphoneTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new SmartphoneTrackingEventManagementPanel(
                getSailingService(), getUserService(), this, this, this, getStringMessages());
        raceLogTrackingEventManagementPanel.ensureDebugId("SmartphoneTrackingPanel");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SmartphoneTrackingEventManagementPanel>(raceLogTrackingEventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
            }
        }, getStringMessages().smartphoneTracking()); // no permissions required; we show those Smartphone connector stuff the user may read
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(getSailingService(), this, getStringMessages());
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<IgtimiAccountsPanel>(igtimiAccountsPanel),
                getStringMessages().igtimiAccounts()); // Igtimi accounts are displayed based on permissions
        
        ExpeditionDeviceConfigurationsPanel expeditionDeviceConfigurationsPanel = new ExpeditionDeviceConfigurationsPanel(getSailingService(), this, getStringMessages());
        expeditionDeviceConfigurationsPanel.ensureDebugId("ExpeditionDeviceConfigurations");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ExpeditionDeviceConfigurationsPanel>(expeditionDeviceConfigurationsPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                expeditionDeviceConfigurationsPanel.refresh();
            }
        }, getStringMessages().expeditionDeviceConfigurations()); // Expedition device configurations are displayed based on individual user permissions

        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(getSailingService(), this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ResultImportUrlsManagementPanel>(resultImportUrlsManagementPanel),
                getStringMessages().resultImportUrls()); // result import URLs have ownerships and are displayed as the user can see / update / delete them
        
        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(
                getSailingService(), getUserService(), this, getStringMessages(), this, eventManagementPanel);
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(structureImportUrlsManagementPanel),
                getStringMessages().manage2Sail() + " " + getStringMessages().regattaStructureImport(),
                SecuredDomainType.REGATTA.getPermission(DefaultActions.CREATE)); // TODO bug4763 provide the default CREATE ownership for REGATTA / EVENT

        /* ADVANCED */
        
        final HorizontalTabLayoutPanel advancedTabPanel = panel.addVerticalTab(getStringMessages().advanced(), "AdvancedPanel");
        advancedTabPanel.ensureDebugId("AdvancedTab");
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
                getStringMessages().masterDataImportPanel(), SecuredSecurityTypes.SERVER.getPermissionForObjects(
                        SecuredSecurityTypes.ServerActions.IMPORT_MASTER_DATA, serverInfo.getServerName()));

        RemoteServerInstancesManagementPanel remoteServerInstancesManagementPanel = new RemoteServerInstancesManagementPanel(getSailingService(), this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<RemoteServerInstancesManagementPanel>(remoteServerInstancesManagementPanel),
                getStringMessages().remoteServerInstances(),
                SecuredSecurityTypes.SERVER.getPermissionForObjects(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_LOCAL_SERVER, serverInfo.getServerName()));

        LocalServerManagementPanel localServerInstancesManagementPanel = new LocalServerManagementPanel(getSailingService(), this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<LocalServerManagementPanel>(localServerInstancesManagementPanel),
                getStringMessages().localServer(),
                SecuredSecurityTypes.SERVER.getPermissionForObjects(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_LOCAL_SERVER, serverInfo.getServerName()));

        final UserManagementPanel<AdminConsoleTableResources> userManagementPanel = new UserManagementPanel<>(getUserService(), StringMessages.INSTANCE,
                SecuredDomainType.getAllInstances(), this, tableResources);
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserManagementPanel<AdminConsoleTableResources>>(userManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userManagementPanel.updateUsersAndACLs();
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

        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanel(getUserService(), StringMessages.INSTANCE);
        panel.addToTabPanel(advancedTabPanel,
                new DefaultRefreshableAdminConsolePanel<UserGroupManagementPanel>(userGroupManagementPanel) {
                    @Override
                    public void refreshAfterBecomingVisible() {
                        userGroupManagementPanel.updateUserGroupsAndUsers();
                    }
                }, getStringMessages().userGroupManagement(), SecuredSecurityTypes.USER_GROUP.getPermission(DefaultActions.MUTATION_ACTIONS));

        final FileStoragePanel fileStoragePanel = new FileStoragePanel(getSailingService(), this);
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<FileStoragePanel>(fileStoragePanel),
                getStringMessages().fileStorage(), SecuredSecurityTypes.SERVER.getPermissionForObjects(
                        SecuredSecurityTypes.ServerActions.CONFIGURE_FILE_STORAGE, serverInfo.getServerName()));
        panel.initUI();
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();
        return panel;
    }

    @Override
    public void fillLeaderboards() {
        getSailingService().getLeaderboards(new MarkedAsyncCallback<List<StrippedLeaderboardDTO>>(
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
