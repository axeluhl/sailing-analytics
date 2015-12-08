package com.sap.sailing.gwt.home.desktop;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.desktop.app.DesktopActivityManager;
import com.sap.sailing.gwt.home.desktop.app.DesktopActivityMapper;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sailing.gwt.home.desktop.app.TabletAndDesktopApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.usermanagement.UserChangeEvent;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.ServerConfigurationService;
import com.sap.sailing.gwt.ui.client.ServerConfigurationServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.gwt.resources.Highcharts;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.UserDTO;

public class DesktopEntryPoint extends AbstractMvpEntryPoint<StringMessages, DesktopClientFactory> {

    private WithSecurity securityProvider;

    @Override
    public void doOnModuleLoad() {
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
        
        CommonControlsCSS.ensureInjected();
        Highcharts.ensureInjected();

        ServerConfigurationServiceAsync serverConfigService = GWT.create(ServerConfigurationService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) serverConfigService, RemoteServiceMappingConstants.serverConfigurationServiceRemotePath);
       
        serverConfigService.isStandaloneServer(new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                createDesktopApplication(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                createDesktopApplication(false);
            }
        });
    }

    private void createDesktopApplication(boolean isStandaloneServer) {
        final DesktopClientFactory clientFactory = new TabletAndDesktopApplicationClientFactory(isStandaloneServer);
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new DesktopActivityMapper(clientFactory));
        securityProvider = new DefaultWithSecurityImpl();
        securityProvider.getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                clientFactory.getEventBus().fireEvent(new UserChangeEvent(user));
            }
        });
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
    
    @Override
    protected ActivityManager createActivityManager(ActivityMapper activityMapperRegistry, DesktopClientFactory clientFactory) {
        DesktopActivityManager sailingActivityManager = new DesktopActivityManager(activityMapperRegistry, clientFactory.getEventBus());
        sailingActivityManager.setNavigationPathDisplay(clientFactory.getNavigationPathDisplay());
        return sailingActivityManager;
    }
}