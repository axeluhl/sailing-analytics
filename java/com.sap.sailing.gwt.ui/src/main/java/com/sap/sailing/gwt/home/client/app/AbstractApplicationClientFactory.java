package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.start.StartPlace;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;


public abstract class AbstractApplicationClientFactory extends ClientFactoryImpl implements ApplicationClientFactory {
    private final SailingServiceAsync sailingService;

    private final TopLevelView root;
    
    public AbstractApplicationClientFactory(TopLevelView root) {
        this.root = root;
        sailingService = GWT.create(SailingServiceAsync.class);
    }
    
    @Override
    public Widget getRoot() {
        return root.asWidget();
    }

    @Override
    public AcceptsOneWidget getStage() {
        return root.getStage();
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
