package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.app.event.EventView;
import com.sap.sailing.gwt.home.client.app.events.EventsActivity;
import com.sap.sailing.gwt.home.client.app.events.EventsView;
import com.sap.sailing.gwt.home.client.app.start.SmartphoneStartView;
import com.sap.sailing.gwt.home.client.app.start.StartView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public class SmartphoneApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public SmartphoneApplicationClientFactory() {
        this(new SimpleEventBus());
    }
    
    private SmartphoneApplicationClientFactory(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private SmartphoneApplicationClientFactory(EventBus eventBus, PlaceController placeController) {
        super(new SmartphoneApplicationView(new MainMenuNavigatorImpl(placeController)), eventBus, placeController);
    }

    @Override
    public EventView createEventView(EventDTO event) {
        // TODO createEventView
        return null;
    }

    @Override
    public EventsView createEventsView(Iterable<EventDTO> events, EventsActivity activity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StartView createStartView() {
        return new SmartphoneStartView();
    }
}
