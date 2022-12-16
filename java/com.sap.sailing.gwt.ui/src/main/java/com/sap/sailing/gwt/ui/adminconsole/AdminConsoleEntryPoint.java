package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.adminconsole.places.DefaultPlace;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleEntryPoint extends AbstractSailingWriteEntryPoint {
    private final SimplePanel appWidget = new SimpleLayoutPanel();

    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        initActivitiesAndPlaces();
    }

    private void initActivitiesAndPlaces() {
        final AdminConsoleClientFactory clientFactory = new AdminConsoleClientFactoryImpl(getSailingService(), this);
        final EventBus eventBus = clientFactory.getEventBus();
        final PlaceController placeController = clientFactory.getPlaceController();
        final AdminConsoleActivityMapper activityMapper = new AdminConsoleActivityMapper(clientFactory);
        final ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);
        final AdminConsolePlaceHistoryMapper historyMapper = GWT.create(AdminConsolePlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new DefaultPlace());
        RootLayoutPanel.get().add(appWidget);
        historyHandler.handleCurrentHistory();
    }
}