package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.app.start.StartPlace;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;


public abstract class AbstractApplicationClientFactory extends ClientFactoryImpl implements ApplicationClientFactory {
    private final SailingServiceAsync sailingService;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root) {
        super(root);
        sailingService = GWT.create(SailingServiceAsync.class);
    }
    
    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }

    @Override
    public SailingServiceAsync getSailingService() {
        return sailingService;
    }
}
