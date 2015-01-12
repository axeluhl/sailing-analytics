package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppActivityMapper;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppHistoryMapper;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlayEntryPoint extends AbstractMvpEntryPoint {

    public void onModuleLoad() {
        AutoPlayAppClientFactory clientFactory = GWT.create(AutoPlayAppClientFactory.class);

        EntryPointHelper.registerASyncService((ServiceDefTarget) clientFactory.getSailingService(), RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) clientFactory.getMediaService(), RemoteServiceMappingConstants.mediaServiceRemotePath);
        
        AutoPlayAppHistoryMapper applicationHistoryMapper = GWT.create(AutoPlayAppHistoryMapper.class);
        onModuleLoad(clientFactory, applicationHistoryMapper, new AutoPlayAppActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        StyleInjector.injectAtEnd("@media (min-width: 25em) { "+SharedResources.INSTANCE.mediumCss().getText()+"}");
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+SharedResources.INSTANCE.largeCss().getText()+"}");
    }
}