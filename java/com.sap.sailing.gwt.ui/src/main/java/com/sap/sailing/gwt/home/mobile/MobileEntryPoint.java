package com.sap.sailing.gwt.home.mobile;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.app.MobileActivityMapper;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.SailingActivityManager;
import com.sap.sailing.gwt.home.shared.usermanagement.UserChangeEvent;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.ServerConfigurationService;
import com.sap.sailing.gwt.ui.client.ServerConfigurationServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.UserDTO;

public class MobileEntryPoint extends AbstractMvpEntryPoint<StringMessages, MobileApplicationClientFactory> {

    private WithSecurity securityProvider;

    @Override
    public void doOnModuleLoad() {
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().mobile());

        CommonControlsCSS.ensureInjected();
        
        ServerConfigurationServiceAsync serverConfigService = GWT.create(ServerConfigurationService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) serverConfigService, RemoteServiceMappingConstants.serverConfigurationServiceRemotePath);
       
        serverConfigService.isStandaloneServer(new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                createMobileApplication(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                createMobileApplication(false);
            }
        });
    }

    private void createMobileApplication(boolean isStandaloneServer) {
        final MobileApplicationClientFactory clientFactory = new MobileApplicationClientFactory(isStandaloneServer);
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new MobileActivityMapper(clientFactory));
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
    protected ActivityManager createActivityManager(ActivityMapper activityMapperRegistry, MobileApplicationClientFactory clientFactory) {
        SailingActivityManager<ResettableNavigationPathDisplay> sailingActivityManager = new SailingActivityManager<>(activityMapperRegistry, clientFactory.getEventBus());
        sailingActivityManager.setNavigationPathDisplay(clientFactory.getNavigationPathDisplay());
        return sailingActivityManager;
    }

}
