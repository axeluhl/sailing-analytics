package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.app.event.EventView;
import com.sap.sailing.gwt.home.client.app.event.TabletAndDesktopEventView;
import com.sap.sailing.gwt.home.client.app.events.EventsActivity;
import com.sap.sailing.gwt.home.client.app.events.EventsView;
import com.sap.sailing.gwt.home.client.app.events.TabletAndDesktopEventsView;
import com.sap.sailing.gwt.home.client.app.start.StartView;
import com.sap.sailing.gwt.home.client.app.start.TabletAndDesktopStartView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public class TabletAndDesktopApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public TabletAndDesktopApplicationClientFactory() {
        this(new SimpleEventBus());
    }
    
    private TabletAndDesktopApplicationClientFactory(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private TabletAndDesktopApplicationClientFactory(EventBus eventBus, PlaceController placeController) {
        super(new TabletAndDesktopApplicationView(new MainMenuNavigatorImpl(placeController)), eventBus, placeController);
    }

    @Override
    public EventView createEventView(EventDTO event) {
        return new TabletAndDesktopEventView(event);
    }

    @Override
    public EventsView createEventsView(Iterable<EventDTO> events, EventsActivity activity) {
        return new TabletAndDesktopEventsView(events, activity);
    }

    @Override
    public StartView createStartView() {
        return new TabletAndDesktopStartView();
    }
}
