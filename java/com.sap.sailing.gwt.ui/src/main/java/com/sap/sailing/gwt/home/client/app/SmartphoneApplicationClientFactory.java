package com.sap.sailing.gwt.home.client.app;

import com.sap.sailing.gwt.home.client.app.event.EventView;
import com.sap.sailing.gwt.home.client.app.events.EventsActivity;
import com.sap.sailing.gwt.home.client.app.events.EventsView;
import com.sap.sailing.gwt.home.client.app.start.SmartphoneStartView;
import com.sap.sailing.gwt.home.client.app.start.StartView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public class SmartphoneApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public SmartphoneApplicationClientFactory() {
        super(new SmartphoneApplicationView());
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
