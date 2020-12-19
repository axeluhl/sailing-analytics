package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleClientFactory;
import com.sap.sailing.gwt.ui.adminconsole.places.refresher.AbstractRefresher;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.Refresher;
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
    private AdminConsoleView adminConsoleView;
    private MediaServiceWriteAsync mediaServiceWrite;
    private SailingServiceWriteAsync sailingService;
    private AbstractAdminConsolePlace defaultPlace;
    
    private final Refresher<StrippedLeaderboardDTOWithSecurity> leaderboardsRefresher;
    private final Refresher<LeaderboardGroupDTO> leaderboardGroupsRefresher;
    private final Refresher<RegattaDTO> regattasRefresher;
    private final Refresher<EventDTO> eventsRefresher;
    private final Refresher<MediaTrackWithSecurityDTO> mediaTracksRefresher;
    
    public AdminConsoleActivity(final AdminConsoleClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = clientFactory.getMediaServiceWrite();
        this.sailingService = clientFactory.getSailingService();
        
        leaderboardsRefresher = new AbstractRefresher<StrippedLeaderboardDTOWithSecurity>(clientFactory.getErrorReporter()) {
            @Override
            public void reload(AsyncCallback<Iterable<StrippedLeaderboardDTOWithSecurity>> callback) {
                sailingService.getLeaderboardsWithSecurity(new MarkedAsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>(
                        new AsyncCallback<List<StrippedLeaderboardDTOWithSecurity>>() {
                            @Override
                            public void onSuccess(List<StrippedLeaderboardDTOWithSecurity> result) {
                                callback.onSuccess(new ArrayList<StrippedLeaderboardDTOWithSecurity>(result));
                            }
                            @Override
                            public void onFailure(Throwable t) {
                                callback.onFailure(t);
                            }
                        }));
            }
        };
        leaderboardGroupsRefresher = new AbstractRefresher<LeaderboardGroupDTO>(clientFactory.getErrorReporter()) {
            @Override
            public void reload(AsyncCallback<Iterable<LeaderboardGroupDTO>> callback) {
                sailingService.getLeaderboardGroups(false /* withGeoLocationData */,
                        new MarkedAsyncCallback<List<LeaderboardGroupDTO>>(new AsyncCallback<List<LeaderboardGroupDTO>>() {
                            @Override
                            public void onSuccess(List<LeaderboardGroupDTO> result) {
                                callback.onSuccess(new ArrayList<LeaderboardGroupDTO>(result));
                            }
                            @Override
                            public void onFailure(Throwable t) {
                                callback.onFailure(t);
                            }
                        }));
            }
        };
        regattasRefresher = new AbstractRefresher<RegattaDTO>(clientFactory.getErrorReporter()) {
            @Override
            public void reload(AsyncCallback<Iterable<RegattaDTO>> callback) {
                sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
                        new AsyncCallback<List<RegattaDTO>>() {
                            @Override
                            public void onSuccess(List<RegattaDTO> result) {
                                callback.onSuccess(new ArrayList<RegattaDTO>(result));
                            }
                            @Override
                            public void onFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }
                        }));
            }
        };
        eventsRefresher = new AbstractRefresher<EventDTO>(clientFactory.getErrorReporter()) {
            @Override
            public void reload(AsyncCallback<Iterable<EventDTO>> callback) {
                sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        callback.onSuccess(new ArrayList<EventDTO>(result));
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }
                });
            }
        };
        mediaTracksRefresher = new AbstractRefresher<MediaTrackWithSecurityDTO>(clientFactory.getErrorReporter()) {
            @Override
            public void reload(AsyncCallback<Iterable<MediaTrackWithSecurityDTO>> callback) {
                mediaServiceWrite.getAllMediaTracks(new AsyncCallback<Iterable<MediaTrackWithSecurityDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(Iterable<MediaTrackWithSecurityDTO> result) {
                        List<MediaTrackWithSecurityDTO> list = new ArrayList<MediaTrackWithSecurityDTO>();
                        result.forEach(mediaTrackDto -> list.add(mediaTrackDto));
                        callback.onSuccess(list);
                    }
                });
            }
        };
    }

    @Override
    public Refresher<StrippedLeaderboardDTOWithSecurity> getLeaderboardsRefresher() {
        return leaderboardsRefresher;
    }
    
    @Override
    public Refresher<LeaderboardGroupDTO> getLeaderboardGroupsRefresher() {
        return leaderboardGroupsRefresher;
    }

    @Override
    public Refresher<RegattaDTO> getRegattasRefresher() {
        return regattasRefresher;
    }

    @Override
    public Refresher<EventDTO> getEventsRefresher() {
        return eventsRefresher;
    }
    
    @Override
    public Refresher<MediaTrackWithSecurityDTO> getMediaTracksRefresher() {
        return mediaTracksRefresher;
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
}
