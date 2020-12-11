package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleClientFactory;
import com.sap.sailing.gwt.ui.client.EventsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.MediaTracksRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;

public class AdminConsoleActivity extends AbstractActivity implements AdminConsoleView.Presenter {
    private AdminConsoleClientFactory clientFactory;
    private final Set<RegattasDisplayer> regattasDisplayers;
    private final Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers;
    private final Set<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;
    private Set<EventsDisplayer> eventsDisplayers;
    private AdminConsoleView adminConsoleView;
    private MediaServiceWriteAsync mediaServiceWrite;
    private SailingServiceWriteAsync sailingService;
    private static AdminConsoleActivity instance;
    private AbstractAdminConsolePlace defaultPlace;
    private MediaTracksRefresher mediaTracksRefresher;
    private List<StrippedLeaderboardDTOWithSecurity> leaderboards;
    private List<LeaderboardGroupDTO> leaderboardGroups;
    private List<RegattaDTO> regattas;
    private List<EventDTO> events;
    
    public static boolean instantiated() {
        return instance != null;
    }
    
    public static AdminConsoleActivity getInstance(final AdminConsoleClientFactory clientFactory) {
        if(instance == null) {
            instance = new AdminConsoleActivity(clientFactory);
        }
        return instance;
    }
    
    public static AdminConsoleActivity getInstance(final AdminConsoleClientFactory clientFactory, AbstractAdminConsolePlace defaultPlace) {
        if(instance == null) {
            instance = new AdminConsoleActivity(clientFactory);
            instance.setRedirectToPlace(defaultPlace);
        }
        return instance;
    }
    
    private AdminConsoleActivity(final AdminConsoleClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = clientFactory.getMediaServiceWrite();
        this.sailingService = clientFactory.getSailingService();
        regattasDisplayers = new HashSet<>();
        leaderboardsDisplayers = new HashSet<>();
        leaderboardGroupsDisplayers = new HashSet<>();
    }
    
    public AdminConsoleActivity(final AdminConsolePlace place, final AdminConsoleClientFactory clientFactory) {
        this(clientFactory); 
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {     
        initView(); 
        containerWidget.setWidget(adminConsoleView.asWidget());    
    }
    
    public void setRedirectToPlace(AbstractAdminConsolePlace place) {
        this.defaultPlace = place;
    }
    
    public void goToMenuAndTab(AbstractAdminConsolePlace place) {
        adminConsoleView.selectTabByPlace(place);       
    }
    
    private void initView() {
        if (adminConsoleView == null) {
            adminConsoleView = new AdminConsoleViewImpl();
            adminConsoleView.setPresenter(this);
            adminConsoleView.setRedirectToPlace(defaultPlace);
            clientFactory.getUserService().executeWithServerInfo(adminConsoleView::createUI);
            clientFactory.getUserService().addUserStatusEventHandler((u, p) -> checkPublicServerNonPublicUserWarning());
        }
    }
    
    @Override
    public UserService getUserService() {
        return clientFactory.getUserService();
    }
    
    @Override
    public SailingServiceWriteAsync getSailingService() {
        return sailingService;
    }
    
    @Override
    public MediaServiceWriteAsync getMediaServiceWrite() {
        return mediaServiceWrite;
    }
    
    @Override
    public ErrorReporter getErrorReporter() {
        return clientFactory.getErrorReporter();
    }
    
    @Override
    public Iterable<RegattasDisplayer> getRegattasDisplayers() {
        return regattasDisplayers;
    }
    
    @Override
    public Iterable<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> getLeaderboardsDisplayers() {
        return leaderboardsDisplayers;
    }
    
    @Override
    public Iterable<LeaderboardGroupsDisplayer> getLeaderboardGroupsDisplayers() {
        return leaderboardGroupsDisplayers;
    }
    
    @Override
    public void addLeaderboardGroupsDisplayer(LeaderboardGroupsDisplayer leaderboardGroupsDisplayer) {
        this.leaderboardGroupsDisplayers.add(leaderboardGroupsDisplayer);
    }

    @Override
    public void addRegattasDisplayer(RegattasDisplayer regattasDisplayer) {
        this.regattasDisplayers.add(regattasDisplayer);
    }

    @Override
    public void addLeaderboardsDisplayer(
            LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer) {
        this.leaderboardsDisplayers.add(leaderboardsDisplayer);
    }

    @Override
    public Iterable<EventsDisplayer> getEventsDisplayers() {
        return eventsDisplayers;
    }
    
    public void addEventsDisplayer(EventsDisplayer eventsDisplayer) {
        this.eventsDisplayers.add(eventsDisplayer);
    }

    @Override
    public void loadLeaderboards() {
        if (!leaderboardsDisplayers.isEmpty()) {
            if (leaderboards == null) {
                reloadLeaderboards();
            } else {
                updateLeaderboardDisplayer();
            }
        }
    }
    
    @Override
    public void loadLeaderboardGroups() {
        if (!leaderboardGroupsDisplayers.isEmpty()) {
            if (leaderboardGroups == null) {
                reloadLeaderboardGroups();
            } else {
                updateLeaderboardGroupDisplayer();
            }
        }
    }


    @Override
    public void loadRegattas() {
        if (!regattasDisplayers.isEmpty()) {
            if (regattas == null) {
                reloadRegattas();
            } else {
                updateRegattaDisplayer();
            }
        }
    }
    
    @Override
    public void loadEvents() {
        if (!eventsDisplayers.isEmpty()) {
            if (events == null) {
                reloadEvents();
            } else {
                updateEventDisplayer();
            }
        }
    }

    @Override
    public void reloadLeaderboards() {
        sailingService.getLeaderboardsWithSecurity(new MarkedAsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>(
                new AsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>() {
                    @Override
                    public void onSuccess(List<StrippedLeaderboardDTOWithSecurity> result) {
                        leaderboards = new ArrayList<StrippedLeaderboardDTOWithSecurity>(result);
                        updateLeaderboardDisplayer();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        clientFactory.getErrorReporter()
                                .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
                    }
                }));
    }

    @Override
    public void reloadRegattas() {
        sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
                new AsyncCallback<List<RegattaDTO>>() {
                    @Override
                    public void onSuccess(List<RegattaDTO> result) {
                        regattas = new ArrayList<RegattaDTO>(result);
                        updateRegattaDisplayer();
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        clientFactory.getErrorReporter().reportError("Remote Procedure Call getRegattas() - Failure");
                    }
                }));
    }
    
    public void reloadEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> result) {
                events = new ArrayList<EventDTO>(result);
                updateEventDisplayer();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                clientFactory.getErrorReporter().reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
            }
        });
    }

    @Override
    public void updateLeaderboards(List<StrippedLeaderboardDTOWithSecurity> updatedLeaderboards) {
        leaderboards = new ArrayList<StrippedLeaderboardDTOWithSecurity>(updatedLeaderboards);
    }

    @Override
    public void updateLeaderboardGroups(List<LeaderboardGroupDTO> updatedLeaderboardGroups) {
        leaderboardGroups = new ArrayList<LeaderboardGroupDTO>(updatedLeaderboardGroups);
    }

    private void updateLeaderboardDisplayer() {
        for (LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer : getLeaderboardsDisplayers()) {
            leaderboardsDisplayer.fillLeaderboards(new ArrayList<StrippedLeaderboardDTOWithSecurity>(leaderboards));
        }
    }
    
    private void updateRegattaDisplayer() {
        for (RegattasDisplayer regattaDisplayer : getRegattasDisplayers()) {
            regattaDisplayer.fillRegattas(new ArrayList<RegattaDTO>(regattas));
        }
    }
    
    private void updateLeaderboardGroupDisplayer() {
        for (LeaderboardGroupsDisplayer leaderboardGroupsDisplayer : getLeaderboardGroupsDisplayers()) {
            leaderboardGroupsDisplayer.fillLeaderboardGroups(leaderboardGroups);
        }
    }
    
    private void updateEventDisplayer() {
        for (final EventsDisplayer eventsDisplayer : getEventsDisplayers()) {
            eventsDisplayer.fillEvents(events);
        }
    }

    @Override
    public void reloadLeaderboardGroups() {
        sailingService.getLeaderboardGroups(false /* withGeoLocationData */,
                new MarkedAsyncCallback<List<LeaderboardGroupDTO>>(new AsyncCallback<List<LeaderboardGroupDTO>>() {
                    @Override
                    public void onSuccess(List<LeaderboardGroupDTO> result) {
                        leaderboardGroups = new ArrayList<LeaderboardGroupDTO>(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        clientFactory.getErrorReporter()
                                .reportError("Error trying to obtain list of leaderboard groups: " + t.getMessage());
                    }
                }));
    }
    
    @Override
    public void setupLeaderboardGroups(LeaderboardGroupsDisplayer displayer, Map<String, String> params) {
        displayer.setupLeaderboardGroups(params);
    }

    protected void checkPublicServerNonPublicUserWarning() {
        sailingService.getServerConfiguration(new AsyncCallback<ServerConfigurationDTO>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ServerConfigurationDTO result) {
                if (Boolean.TRUE.equals(result.isPublic())) {
                    StrippedUserGroupDTO currentTenant = clientFactory.getUserService().getCurrentTenant();
                    StrippedUserGroupDTO serverTenant = result.getServerDefaultTenant();
                    if (!serverTenant.equals(currentTenant) && clientFactory.getUserService().getCurrentUser() != null) {
                        if (clientFactory.getUserService().getCurrentUser().getUserGroups().contains(serverTenant)) {
                            // The current user is in server tenant group and so his default tenant could be changed.
                            if (Window.confirm(StringMessages.INSTANCE.serverIsPublicButTenantIsNotAndCouldBeChanged())) {
                                // change the default tenant
                                changeDefaultTenantForCurrentUser(serverTenant);
                            }
                        } else {
                            // The current user is not in the server tenant group so his default tenant cannot be
                            // changed.
                            Window.alert(StringMessages.INSTANCE.serverIsPublicButTenantIsNot());
                        }
                    }
                }
            }

            /** Changes the default tenant for the current user. */
            private void changeDefaultTenantForCurrentUser(final StrippedUserGroupDTO serverTenant) {
                final UserDTO user = clientFactory.getUserService().getCurrentUser();
                clientFactory.getUserManagementWriteService().updateUserProperties(user.getName(), user.getFullName(),
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

    @Override
    public PlaceController getPlaceController() {
        return clientFactory.getPlaceController();
    }

    @Override
    public void loadMediaTracks() {
        if (mediaTracksRefresher != null) {
            mediaTracksRefresher.loadMediaTracks();
        }
    }
    
    public void setMediaTracksRefresher(MediaTracksRefresher mediaTracksRefresher) {
        this.mediaTracksRefresher = mediaTracksRefresher;
    }

 
}
