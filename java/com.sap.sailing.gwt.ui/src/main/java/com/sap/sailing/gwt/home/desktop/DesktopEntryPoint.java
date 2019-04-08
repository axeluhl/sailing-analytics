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
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.ServerConfigurationService;
import com.sap.sailing.gwt.ui.client.ServerConfigurationServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

/**
 * Desktop/tablet EntryPoint for the Home module. Be aware that this EntryPoint isn't directly started but instead the
 * {@link SwitchingEntryPoint} decides about which version of the application will be shown.
 */
public class DesktopEntryPoint extends AbstractMvpEntryPoint<StringMessages, DesktopClientFactory> {


    @Override
    public void doOnModuleLoad() {
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
        
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