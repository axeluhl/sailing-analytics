package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.client.app.ApplicationActivityMapper;
import com.sap.sailing.gwt.home.client.app.ApplicationClientFactory;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class HomeEntryPoint extends AbstractMvpEntryPoint<StringMessages> {
    @Override
    public void doOnModuleLoad() {
        ApplicationClientFactory clientFactory = GWT.create(ApplicationClientFactory.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) clientFactory.getSailingService(), RemoteServiceMappingConstants.sailingServiceRemotePath);
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new ApplicationActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        StyleInjector.injectAtEnd("@media (min-width: 25em) { "+SharedResources.INSTANCE.mediumCss().getText()+"}");
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+SharedResources.INSTANCE.largeCss().getText()+"}");
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}