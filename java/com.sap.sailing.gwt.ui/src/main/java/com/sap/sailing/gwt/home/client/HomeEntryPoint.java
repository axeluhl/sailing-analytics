package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.app.ApplicationActivityMapper;
import com.sap.sailing.gwt.home.client.app.ApplicationClientFactory;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sse.gwt.client.mvp.AbstractEntryPoint;

public class HomeEntryPoint extends AbstractEntryPoint {

    public void onModuleLoad() {
        ApplicationClientFactory clientFactory = GWT.create(ApplicationClientFactory.class);
        onModuleLoad(clientFactory.getStage(), clientFactory.getRoot(), clientFactory.getDefaultPlace(), clientFactory,
                ApplicationHistoryMapper.class, new ApplicationActivityMapper(clientFactory));
    }
}