package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppActivityMapper;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppHistoryMapper;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlayEntryPoint extends AbstractMvpEntryPoint<StringMessages, AutoPlayAppClientFactory> {

    @Override
    public void doOnModuleLoad() {
        AutoPlayAppClientFactory clientFactory = GWT.create(AutoPlayAppClientFactory.class);
        
        AutoPlayAppHistoryMapper applicationHistoryMapper = GWT.create(AutoPlayAppHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayAppActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}