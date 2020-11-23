package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleEntryPoint extends AbstractSailingWriteEntryPoint {    
    
    // TODO sarah
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);
    
    private SimplePanel appWidget = new SimplePanel();
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
<<<<<<< HEAD
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        getUserService().executeWithServerInfo(this::createUI);
        getUserService().addUserStatusEventHandler((u, p) -> checkPublicServerNonPublicUserWarning());
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
                    if (!serverTenant.equals(currentTenant) && getUserService().getCurrentUser() != null) {
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
                getUserManagementWriteService().updateUserProperties(user.getName(), user.getFullName(),
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
=======
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceWriteRemotePath, HEADER_FORWARD_TO_MASTER);
>>>>>>> bug5288: new single Place and View for AdminConsole
        
        //getUserService().executeWithServerInfo(this::createUI);
        //getUserService().addUserStatusEventHandler((u, p) -> checkPublicServerNonPublicUserWarning());
        
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final AdminConsoleClientFactory clientFactory = GWT.create(AdminConsoleClientFactoryImpl.class);
        EventBus eventBus = clientFactory.getEventBus();
        clientFactory.setSailingService(getSailingService());
        PlaceController placeController = clientFactory.getPlaceController();
        
        ActivityMapper activityMapper = new AdminConsoleActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);
        
        AdminConsolePlaceHistoryMapper historyMapper = GWT.create(AdminConsolePlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new AdminConsolePlace());
        
        RootLayoutPanel.get().add(appWidget);
        
        historyHandler.handleCurrentHistory();
    }

}