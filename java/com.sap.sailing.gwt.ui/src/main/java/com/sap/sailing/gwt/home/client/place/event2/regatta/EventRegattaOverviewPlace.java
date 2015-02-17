package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRegattaOverviewPlace extends AbstractEventRegattaPlace {

    public EventRegattaOverviewPlace(EventContext ctx) {
        super(ctx);
    }

    public EventRegattaOverviewPlace(String eventUuidAsString, String regattaUuidAsString) {
        super(eventUuidAsString, regattaUuidAsString);
    }
}
