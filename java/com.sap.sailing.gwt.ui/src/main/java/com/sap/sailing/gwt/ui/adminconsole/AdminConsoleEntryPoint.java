package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
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
import com.sap.sailing.gwt.ui.client.filestorage.FileStoragePanel;
import com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportPanel;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.DefaultRefreshableAdminConsolePanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.component.UserManagementPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AdminConsoleEntryPoint extends AbstractSailingEntryPoint implements RegattaRefresher, LeaderboardsRefresher, LeaderboardGroupsRefresher {
    private Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer> leaderboardsDisplayers;
    private Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;

    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
        createUI();
    }
     
    private void createUI() {
        AdminConsolePanel panel = new AdminConsolePanel(getUserService(), sailingService, 
                getStringMessages().releaseNotes(), "/release_notes_admin.html", /* error reporter */ this, SecurityStylesheetResources.INSTANCE.css());
        BetterDateTimeBox.initialize();
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();

        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService, this, getStringMessages());
        panel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                fillLeaderboardGroups();
            }
        }, getStringMessages().events(), SailingAdminConsoleFeatures.MANAGE_EVENTS);
        leaderboardGroupsDisplayers.add(eventManagementPanel);

        RegattaManagementPanel regattaManagementPanel = new RegattaManagementPanel(
                sailingService, this, getStringMessages(), this);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        panel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<RegattaManagementPanel>(regattaManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillRegattas();
            }
        }, getStringMessages().regattas(), SailingAdminConsoleFeatures.MANAGE_REGATTAS);
        regattasDisplayers.add(regattaManagementPanel);
        
        /* LEADERBOARDS */
        
        final TabLayoutPanel leaderboardTabPanel = panel.addVerticalTab(getStringMessages().leaderboards(),
                "LeaderboardPanel", SailingAdminConsoleFeatures.MANAGE_LEADERBOARDS);
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, this, this,
                getStringMessages(), /* showRaceDetails */true, this);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        panel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardConfigPanel>(leaderboardConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
            }
        }, getStringMessages().leaderboardConfiguration(), SailingAdminConsoleFeatures.MANAGE_LEADERBOARDS);
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);

        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(sailingService,
                this, this, this, this, getStringMessages());
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        panel.addToTabPanel(leaderboardTabPanel, new DefaultRefreshableAdminConsolePanel<LeaderboardGroupConfigPanel>(leaderboardGroupConfigPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
                fillLeaderboardGroups();
            }
        }, getStringMessages().leaderboardGroups(), SailingAdminConsoleFeatures.MANAGE_LEADERBOARD_GROUPS);
        regattasDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardGroupsDisplayers.add(leaderboardGroupConfigPanel);
        leaderboardsDisplayers.add(leaderboardGroupConfigPanel);

        /* RACES */
        
        final TabLayoutPanel racesTabPanel = panel.addVerticalTab(getStringMessages().races(),
                "RacesPanel", SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        TrackedRacesManagementPanel trackedRacesManagementPanel = new TrackedRacesManagementPanel(sailingService, this,
                this, getStringMessages());
        trackedRacesManagementPanel.ensureDebugId("TrackedRacesManagement");
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<TrackedRacesManagementPanel>(trackedRacesManagementPanel),
                getStringMessages().trackedRaces(), SailingAdminConsoleFeatures.SHOW_TRACKED_RACES);
        regattasDisplayers.add(trackedRacesManagementPanel);

        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, getStringMessages(), this);
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<CompetitorPanel>(competitorPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().refreshCompetitorList();
            }
        }, getStringMessages().competitors(), SailingAdminConsoleFeatures.MANAGE_ALL_COMPETITORS);

        RaceCourseManagementPanel raceCourseManagementPanel = new RaceCourseManagementPanel(sailingService, this, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<RaceCourseManagementPanel>(raceCourseManagementPanel), getStringMessages().courseLayout(), SailingAdminConsoleFeatures.MANAGE_COURSE_LAYOUT);
        regattasDisplayers.add(raceCourseManagementPanel);

        final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();

        WindPanel windPanel = new WindPanel(sailingService, asyncActionsExecutor, this, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<WindPanel>(windPanel), getStringMessages().wind(),
                SailingAdminConsoleFeatures.MANAGE_WIND);
        regattasDisplayers.add(windPanel);

        final MediaPanel mediaPanel = new MediaPanel(regattasDisplayers, sailingService, this, mediaService, this, getStringMessages());
        panel.addToTabPanel(racesTabPanel, new DefaultRefreshableAdminConsolePanel<MediaPanel>(mediaPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().onShow();
            }
        }, getStringMessages().mediaPanel(), SailingAdminConsoleFeatures.MANAGE_MEDIA);

        /* CONNECTORS */
        
        final TabLayoutPanel connectorsTabPanel = panel.addVerticalTab(getStringMessages().connectors(),
                "TrackingProviderPanel", SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(sailingService,
                this, this, getStringMessages());
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<TracTracEventManagementPanel>(tractracEventManagementPanel),
                getStringMessages().tracTracEvents(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(tractracEventManagementPanel);

        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(
                sailingService, this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingReplayConnectorPanel>(swissTimingReplayConnectorPanel),
                getStringMessages().swissTimingArchiveConnector(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swissTimingReplayConnectorPanel);

        SwissTimingEventManagementPanel swisstimingEventManagementPanel = new SwissTimingEventManagementPanel(
                sailingService, this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<SwissTimingEventManagementPanel>(swisstimingEventManagementPanel),
                getStringMessages().swissTimingEvents(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(swisstimingEventManagementPanel);

        final RaceLogTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new RaceLogTrackingEventManagementPanel(
                sailingService, this, this, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<RaceLogTrackingEventManagementPanel>(raceLogTrackingEventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                fillLeaderboards();
            }
        }, getStringMessages().raceLogTracking(), SailingAdminConsoleFeatures.MANAGE_TRACKED_RACES);
        regattasDisplayers.add(raceLogTrackingEventManagementPanel);
        leaderboardsDisplayers.add(raceLogTrackingEventManagementPanel);

        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(sailingService, this, getStringMessages());
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<IgtimiAccountsPanel>(igtimiAccountsPanel),
                getStringMessages().igtimiAccounts(), SailingAdminConsoleFeatures.MANAGE_IGTIMI_ACCOUNTS);
        
        ResultImportUrlsManagementPanel resultImportUrlsManagementPanel = new ResultImportUrlsManagementPanel(sailingService, this, getStringMessages());
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<ResultImportUrlsManagementPanel>(resultImportUrlsManagementPanel),
                getStringMessages().resultImportUrls(), SailingAdminConsoleFeatures.MANAGE_RESULT_IMPORT_URLS);
        
        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(sailingService, this, getStringMessages(), this, eventManagementPanel);
        panel.addToTabPanel(connectorsTabPanel, new DefaultRefreshableAdminConsolePanel<StructureImportManagementPanel>(structureImportUrlsManagementPanel),
                getStringMessages().structureImportUrls(), SailingAdminConsoleFeatures.MANAGE_STRUCTURE_IMPORT_URLS);

        /* ADVANCED */
        
        final TabLayoutPanel advancedTabPanel = panel.addVerticalTab(getStringMessages().advanced(),
                "AdvancedPanel", SailingAdminConsoleFeatures.MANAGE_REPLICATION);
        final ReplicationPanel replicationPanel = new ReplicationPanel(sailingService, this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<ReplicationPanel>(replicationPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                replicationPanel.updateReplicaList();
            }
        }, getStringMessages().replication(), SailingAdminConsoleFeatures.MANAGE_REPLICATION);

        final MasterDataImportPanel masterDataImportPanel = new MasterDataImportPanel(getStringMessages(), sailingService,
                this, eventManagementPanel, this, this);
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<MasterDataImportPanel>(masterDataImportPanel),
                getStringMessages().masterDataImportPanel(), SailingAdminConsoleFeatures.MANAGE_MASTERDATA_IMPORT);

        RemoteSailingServerInstancesManagementPanel sailingServerInstancesManagementPanel = new RemoteSailingServerInstancesManagementPanel(sailingService, this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<RemoteSailingServerInstancesManagementPanel>(sailingServerInstancesManagementPanel),
                getStringMessages().sailingServers(), SailingAdminConsoleFeatures.MANAGE_SAILING_SERVER_INSTANCES);

        final DeviceConfigurationUserPanel deviceConfigurationUserPanel = new DeviceConfigurationUserPanel(sailingService,
                getStringMessages(), this);
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<DeviceConfigurationUserPanel>(deviceConfigurationUserPanel),
                getStringMessages().deviceConfiguration(), SailingAdminConsoleFeatures.MANAGE_DEVICE_CONFIGURATION);

        final UserManagementPanel userManagementPanel = new UserManagementPanel(getUserService(), StringMessages.INSTANCE);
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<UserManagementPanel>(userManagementPanel),
                getStringMessages().userManagement(), SailingAdminConsoleFeatures.MANAGE_USERS);
        
        final FileStoragePanel fileStoragePanel = new FileStoragePanel(sailingService, this, getStringMessages());
        panel.addToTabPanel(advancedTabPanel, new DefaultRefreshableAdminConsolePanel<FileStoragePanel>(fileStoragePanel),
                getStringMessages().fileStorage(), SailingAdminConsoleFeatures.MANAGE_FILE_STORAGE);

        panel.initUI();
        fillRegattas();
        fillLeaderboardGroups();
        fillLeaderboards();
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        rootPanel.add(panel);
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
