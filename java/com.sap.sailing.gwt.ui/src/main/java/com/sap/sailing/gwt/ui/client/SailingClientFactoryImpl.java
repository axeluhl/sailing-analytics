package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;

public abstract class SailingClientFactoryImpl extends ClientFactoryImpl implements SailingClientFactory {
    private final SailingServiceAsync sailingService;

    public SailingClientFactoryImpl(TopLevelView root) {
        super(root);
        sailingService = GWT.create(SailingServiceAsync.class);
    }
    
    @Override
    public SailingServiceAsync getSailingService() {
        return sailingService;
    }

}
