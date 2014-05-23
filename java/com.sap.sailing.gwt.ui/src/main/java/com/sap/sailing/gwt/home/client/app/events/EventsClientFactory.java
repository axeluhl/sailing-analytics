package com.sap.sailing.gwt.home.client.app.events;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventsClientFactory extends SailingClientFactory {
    EventsView createEventsView(Iterable<EventDTO> events, EventsActivity activity);
}
