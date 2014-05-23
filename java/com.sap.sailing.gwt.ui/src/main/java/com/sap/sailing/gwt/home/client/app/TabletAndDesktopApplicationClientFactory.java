package com.sap.sailing.gwt.home.client.app;

import com.sap.sailing.gwt.home.client.app.event.EventView;
import com.sap.sailing.gwt.home.client.app.event.TabletAndDesktopEventView;
import com.sap.sailing.gwt.home.client.app.events.EventsActivity;
import com.sap.sailing.gwt.home.client.app.events.EventsView;
import com.sap.sailing.gwt.home.client.app.events.TabletAndDesktopEventsView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public class TabletAndDesktopApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public TabletAndDesktopApplicationClientFactory() {
        super(new TabletAndDesktopApplicationView());
    }

    @Override
    public EventView createEventView(EventDTO event) {
        return new TabletAndDesktopEventView(event);
    }

    @Override
    public EventsView createEventsView(Iterable<EventDTO> events, EventsActivity activity) {
        return new TabletAndDesktopEventsView(events, activity);
    }
}
