package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;

public class ManagementConsoleEntryPoint extends AbstractSailingWriteEntryPoint {

    private final SimplePanel appWidget = new SimplePanel();

    @Override
    protected void doOnModuleLoad() {
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        ManagementConsoleResources.INSTANCE.icons().ensureInjected();
        ManagementConsoleResources.INSTANCE.style().ensureInjected();

        super.doOnModuleLoad();
        initActivitiesAndPlaces();
    }

    private void initActivitiesAndPlaces() {
        final EventBus eventBus = new SimpleEventBus();
        final SailingServiceWriteAsync service = getSailingService();
        final ManagementConsoleClientFactory clientFactory = new ManagementConsoleClientFactoryImpl(eventBus, service);

        final ManagementConsolePlaceHistoryMapper historyMapper = GWT.create(ManagementConsolePlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(clientFactory.getPlaceController(), eventBus, new ShowcasePlace());

        final ManagementConsoleActivityMapper activityMapper = new ManagementConsoleActivityMapper(clientFactory);
        final ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);

        RootPanel.get().add(appWidget);

        historyHandler.handleCurrentHistory();
    }

}