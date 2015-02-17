package com.sap.sailing.gwt.home.client.place.event2.multiregatta;

import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class AbstractMultiregattaEventPlace extends AbstractEventPlace {

    public AbstractMultiregattaEventPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractMultiregattaEventPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

}
