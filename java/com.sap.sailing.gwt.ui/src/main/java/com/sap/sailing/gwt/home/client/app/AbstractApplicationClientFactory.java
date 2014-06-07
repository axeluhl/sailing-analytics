package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;


public abstract class AbstractApplicationClientFactory extends ClientFactoryImpl implements ApplicationClientFactory {
    private final SailingServiceAsync sailingService;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        sailingService = GWT.create(SailingService.class);
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
