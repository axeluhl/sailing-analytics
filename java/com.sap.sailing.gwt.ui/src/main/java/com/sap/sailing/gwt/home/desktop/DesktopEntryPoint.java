package com.sap.sailing.gwt.home.desktop;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.desktop.app.ApplicationActivityMapper;
import com.sap.sailing.gwt.home.desktop.app.ApplicationClientFactory;
import com.sap.sailing.gwt.home.desktop.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class DesktopEntryPoint extends AbstractMvpEntryPoint<StringMessages> {
    @Override
    public void doOnModuleLoad() {
        ApplicationClientFactory clientFactory = GWT.create(ApplicationClientFactory.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) clientFactory.getSailingService(), RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) clientFactory.getHomeService(), RemoteServiceMappingConstants.homeServiceRemotePath);
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new ApplicationActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}