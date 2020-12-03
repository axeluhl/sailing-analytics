package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.managementconsole.partials.header.Header;
import com.sap.sailing.gwt.managementconsole.partials.mainframe.MainFrame;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ManagementConsoleEntryPoint extends AbstractSailingWriteEntryPoint {

    private final StringMessages msg = StringMessages.INSTANCE;

    @Override
    protected void doOnModuleLoad() {
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        ManagementConsoleResources.INSTANCE.icons().ensureInjected();
        ManagementConsoleResources.INSTANCE.style().ensureInjected();

        super.doOnModuleLoad();
        final EventBus eventBus = new SimpleEventBus();
        final SailingServiceWriteAsync service = getSailingService();
        final ManagementConsoleClientFactory clientFactory = new ManagementConsoleClientFactoryImpl(eventBus, service);
        final MainFrame mainFrame = new MainFrame();
        initActivitiesAndPlaces(clientFactory, eventBus, mainFrame);
        initMainMenuItems(clientFactory.getPlaceController(), mainFrame.getHeader());
    }

    private void initActivitiesAndPlaces(final ManagementConsoleClientFactory clientFactory,
            final EventBus eventBus, final MainFrame mainFrame) {
        final ManagementConsolePlaceHistoryMapper historyMapper = GWT.create(ManagementConsolePlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(clientFactory.getPlaceController(), eventBus, new ShowcasePlace());

        final ManagementConsoleActivityMapper activityMapper = new ManagementConsoleActivityMapper(clientFactory);
        final ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);

        activityManager.setDisplay(mainFrame.getContentContainer());
        RootLayoutPanel.get().add(mainFrame);

        historyHandler.handleCurrentHistory();
    }

    private void initMainMenuItems(final PlaceController placeController, final Header header) {
        header.addMenuItem("SHOWCASE", event -> placeController.goTo(new ShowcasePlace()));
        header.addMenuItem(msg.events(), event -> placeController.goTo(new EventOverviewPlace()));
        header.addMenuItem(msg.deviceConfiguration(), event -> {
        });
        header.addMenuItem(msg.connectors(), event -> {
        });
        header.addMenuItem(msg.courseCreation(), event -> {
        });
        header.addMenuItem(msg.advanced(), event -> {
        });
    }

}